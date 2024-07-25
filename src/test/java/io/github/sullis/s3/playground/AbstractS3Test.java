package io.github.sullis.s3.playground;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.Upload;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;

import static org.assertj.core.api.Assertions.assertThat;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractS3Test {
  private static final String BUCKET_PREFIX = "test-bucket-";
  private static final int PART_SIZE = 5 * 1024 * 1024;
  private static final int NUM_PARTS = 3;
  private static final long EXPECTED_OBJECT_SIZE = NUM_PARTS * PART_SIZE;

  private static final List<SdkAsyncHttpClient.Builder<?>> ASYNC_HTTP_CLIENT_BUILDER_LIST =
      List.of(NettyNioAsyncHttpClient.builder(), AwsCrtAsyncHttpClient.builder());

  private static final List<SdkHttpClient.Builder<?>> SYNC_HTTP_CLIENT_BUILDER_LIST =
      List.of(ApacheHttpClient.builder(), AwsCrtHttpClient.builder());

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected abstract List<ObjectStorageProvider> objectStorageProviders();

  public List<S3AsyncClientInfo> s3AsyncClients() {
    List<S3AsyncClientInfo> result = new ArrayList<>();
    for (ObjectStorageProvider objectStorage : objectStorageProviders()) {
      ASYNC_HTTP_CLIENT_BUILDER_LIST.forEach(httpClientBuilder -> {
        var httpClient = httpClientBuilder.build();
        S3AsyncClient s3Client =
            (S3AsyncClient) objectStorage.configure(S3AsyncClient.builder().httpClient(httpClient)).build();
        result.add(new S3AsyncClientInfo(httpClient.clientName(), objectStorage, s3Client));
      });

      // S3 crtBuilder
      S3CrtAsyncClientBuilder crtBuilder =
          S3AsyncClient.crtBuilder().checksumValidationEnabled(true).maxConcurrency(3).targetThroughputInGbps(0.5)
              .minimumPartSizeInBytes(1_000_000L);
      result.add(new S3AsyncClientInfo("crtBuilder", objectStorage, objectStorage.configure(crtBuilder).build()));
    }

    return result;
  }

  public List<S3ClientInfo> s3Clients() {
    List<S3ClientInfo> result = new ArrayList<>();
    for (ObjectStorageProvider objectStorageProvider : objectStorageProviders()) {
      SYNC_HTTP_CLIENT_BUILDER_LIST.forEach(httpClientBuilder -> {
        var httpClient = httpClientBuilder.build();
        S3Client s3Client =
            (S3Client) objectStorageProvider.configure(S3Client.builder().httpClient(httpClient)).build();
        result.add(new S3ClientInfo(httpClient.clientName(), objectStorageProvider, s3Client));
      });
    }

    return result;
  }

  private Stream<Arguments> s3AsyncClientArguments() {
    List<Arguments> argumentsList = new ArrayList<>();
      for (S3AsyncClientInfo s3AsyncClient : s3AsyncClients()) {
        argumentsList.add(Arguments.of(s3AsyncClient));
      }
    return argumentsList.stream();
  }

  private Stream<Arguments> s3ClientArguments() {
    List<Arguments> argumentsList = new ArrayList<>();
      for (S3ClientInfo s3Client : s3Clients()) {
        argumentsList.add(Arguments.of(s3Client));
      }
    return argumentsList.stream();
  }

  @ParameterizedTest
  @MethodSource("s3AsyncClientArguments")
  public void validateS3AsyncClient(S3AsyncClientInfo s3ClientInfo)
      throws Exception {
    final S3AsyncClient s3Client = s3ClientInfo.client;
    final String bucket = BUCKET_PREFIX + UUID.randomUUID();

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucket);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest).get();
    assertSuccess(createBucketResponse);

    final String key = "key-" + UUID.randomUUID();
    CreateMultipartUploadRequest createMultipartUploadRequest =
        CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build();
    CreateMultipartUploadResponse createMultipartUploadResponse =
        s3Client.createMultipartUpload(createMultipartUploadRequest).get();
    assertSuccess(createMultipartUploadResponse);

    final String uploadId = createMultipartUploadResponse.uploadId();

    List<CompletedPart> completedParts = new ArrayList<>();
    final String partText = StringUtils.repeat("a", PART_SIZE);

    for (int part = 1; part <= NUM_PARTS; part++) {
      AsyncRequestBody requestBody = AsyncRequestBody.fromString(partText);
      UploadPartRequest uploadPartRequest =
          UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId).partNumber(part).build();
      UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody).get();
      assertSuccess(uploadPartResponse);
      logger.info("uploaded part " + part + " via " + s3ClientInfo);
      completedParts.add(CompletedPart.builder().partNumber(part).eTag(uploadPartResponse.eTag()).build());
    }

    CompletedMultipartUpload completedMultipartUpload =
        CompletedMultipartUpload.builder().parts(completedParts).build();

    CompleteMultipartUploadRequest completeMultipartUploadRequest =
        CompleteMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId)
            .multipartUpload(completedMultipartUpload).build();
    CompleteMultipartUploadResponse completeMultipartUploadResponse =
        s3Client.completeMultipartUpload(completeMultipartUploadRequest).get();
    assertSuccess(completeMultipartUploadResponse);

    Path localPath = Path.of(Files.temporaryFolderPath() + "/" + UUID.randomUUID().toString());
    File localFile = localPath.toFile();
    localFile.deleteOnExit();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    GetObjectResponse getObjectResponse = s3Client.getObject(getObjectRequest, localFile.toPath()).get();
    assertSuccess(getObjectResponse);
    assertThat(localFile).exists();
    assertThat(localFile).hasSize(EXPECTED_OBJECT_SIZE);

    InputStream inputStream = s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBlockingInputStream()).get();
    assertThat(inputStream).hasContent(IOUtils.toString(localFile.toURI(), StandardCharsets.UTF_8));

    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucket).build();
    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request).get();
    assertSuccess(listObjectsV2Response);
    List<S3Object> s3Objects = listObjectsV2Response.contents();
    assertThat(s3Objects).hasSize(1);
    S3Object s3Object = s3Objects.get(0);
    assertThat(s3Object.key()).isEqualTo(key);
    assertThat(s3Object.eTag()).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("s3ClientArguments")
  public void validateS3Client(S3ClientInfo s3ClientInfo)
      throws Exception {
    final S3Client s3Client = s3ClientInfo.client;
    final String bucket = createNewBucket(s3Client);

    final String key = "key-" + UUID.randomUUID();
    CreateMultipartUploadRequest createMultipartUploadRequest =
        CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build();
    CreateMultipartUploadResponse createMultipartUploadResponse =
        s3Client.createMultipartUpload(createMultipartUploadRequest);
    assertSuccess(createMultipartUploadResponse);

    final String uploadId = createMultipartUploadResponse.uploadId();

    List<CompletedPart> completedParts = new ArrayList<>();
    final String partText = StringUtils.repeat("a", PART_SIZE);

    for (int part = 1; part <= NUM_PARTS; part++) {
      RequestBody requestBody = RequestBody.fromString(partText);
      UploadPartRequest uploadPartRequest =
          UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId).partNumber(part).build();
      UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
      assertSuccess(uploadPartResponse);
      logger.info("uploaded part " + part + " via " + s3ClientInfo);
      completedParts.add(CompletedPart.builder().partNumber(part).eTag(uploadPartResponse.eTag()).build());
    }

    CompletedMultipartUpload completedMultipartUpload =
        CompletedMultipartUpload.builder().parts(completedParts).build();

    CompleteMultipartUploadRequest completeMultipartUploadRequest =
        CompleteMultipartUploadRequest.builder().bucket(bucket).key(key).uploadId(uploadId)
            .multipartUpload(completedMultipartUpload).build();
    CompleteMultipartUploadResponse completeMultipartUploadResponse =
        s3Client.completeMultipartUpload(completeMultipartUploadRequest);
    assertSuccess(completeMultipartUploadResponse);

    Path localPath = Path.of(Files.temporaryFolderPath() + "/" + UUID.randomUUID().toString());
    File localFile = localPath.toFile();
    localFile.deleteOnExit();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    GetObjectResponse getObjectResponse = s3Client.getObject(getObjectRequest, localFile.toPath());
    assertSuccess(getObjectResponse);
    assertThat(localFile).exists();
    assertThat(localFile).hasSize(EXPECTED_OBJECT_SIZE);

    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucket).build();
    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request);
    assertSuccess(listObjectsV2Response);
    List<S3Object> s3Objects = listObjectsV2Response.contents();
    assertThat(s3Objects).hasSize(1);
    S3Object s3Object = s3Objects.get(0);
    assertThat(s3Object.key()).isEqualTo(key);
    assertThat(s3Object.eTag()).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("s3AsyncClientArguments")
  public void validateTransferManager(S3AsyncClientInfo s3ClientInfo)
      throws Exception {
    final S3AsyncClient s3Client = s3ClientInfo.client;
    final String bucket = createNewBucket(s3Client);
    final String uploadKey = UUID.randomUUID().toString();
    final String payload = "Hello world";

    final LoggingTransferListener listener = LoggingTransferListener.create();

    try (S3TransferManager transferManager = S3TransferManager.builder().s3Client(s3Client).build()) {

      Upload upload = transferManager.upload(
          uploadReq -> uploadReq.requestBody(AsyncRequestBody.fromString(payload)).addTransferListener(listener)
              .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(uploadKey).build()));
      CompletedUpload completedUpload = upload.completionFuture().get();

      PutObjectResponse putObjectResponse = completedUpload.response();
      assertSuccess(putObjectResponse);
      assertThat(putObjectResponse.eTag()).isNotNull();
      assertThat(putObjectResponse.expiration()).isNull();

      File destinationFile = Files.newTemporaryFile();

      DownloadFileRequest downloadFileRequest =
          DownloadFileRequest.builder().addTransferListener(listener).destination(destinationFile)
              .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(uploadKey).build()).build();

      FileDownload fileDownload = transferManager.downloadFile(downloadFileRequest);
      CompletedFileDownload completedFileDownload = fileDownload.completionFuture().get();
      GetObjectResponse getObjectResponse = completedFileDownload.response();
      assertSuccess(getObjectResponse);

      assertThat(destinationFile).isFile();
      assertThat(destinationFile).hasSize(11);

      assertThat(destinationFile).content()
          .isEqualTo(payload);
    }
  }

  private static String createNewBucket(final S3Client s3Client) {
    final String bucket = BUCKET_PREFIX + UUID.randomUUID();

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucket);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
    assertSuccess(createBucketResponse);
    return bucket;
  }

  private static String createNewBucket(S3AsyncClient s3Client)
      throws ExecutionException, InterruptedException {

    final String bucket = BUCKET_PREFIX + UUID.randomUUID();

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucket);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest).get();
    assertSuccess(createBucketResponse);
    return bucket;
  }

  private static void assertSuccess(final SdkResponse sdkResponse) {
    assertThat(sdkResponse.sdkHttpResponse().isSuccessful()).isTrue();
  }

  public record S3AsyncClientInfo(String httpClientDescription, ObjectStorageProvider objectStorageProvider,
                                  S3AsyncClient client) {
    @Override
    public String toString() {
      return objectStorageProvider.getClass().getSimpleName() + ":" + httpClientDescription + ":"
          + this.client.getClass().getSimpleName();
    }
  }

  public record S3ClientInfo(String httpClientDescription, ObjectStorageProvider objectStorageProvider,
                             S3Client client) {
    @Override
    public String toString() {
      return objectStorageProvider.getClass().getSimpleName() + ":" + httpClientDescription + ":"
          + this.client.getClass().getSimpleName();
    }
  }
}