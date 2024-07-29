package io.github.sullis.s3.playground;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
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
import software.amazon.awssdk.services.s3.model.StorageClass;
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
import org.jspecify.annotations.Nullable;


public class S3TestHelper {
  private static final String BUCKET_PREFIX = "test-bucket-";
  private static final int PART_SIZE = 5 * 1024 * 1024;
  private static final int NUM_PARTS = 3;
  private static final long EXPECTED_OBJECT_SIZE = NUM_PARTS * PART_SIZE;

  private static final Logger logger = LoggerFactory.getLogger(S3TestHelper.class);

  static public void validateS3AsyncClient(
      S3AsyncClient s3Client,
      @Nullable StorageClass storageClass)
      throws Exception {
    logger.info("validate S3AsyncClient: storageClass=" + storageClass);
    final String bucket = createNewBucket(s3Client);
    putObjectIntoBucket(s3Client, bucket, storageClass);
    uploadMultiPartIntoBucket(s3Client, bucket);
    exerciseTransferManager(s3Client, storageClass);
  }

  static public void uploadMultiPartIntoBucket(S3AsyncClient s3Client, String bucket) throws Exception {

    final String key = "multipart-key-" + UUID.randomUUID();
    CreateMultipartUploadRequest createMultipartUploadRequest =
        CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build();
    CreateMultipartUploadResponse createMultipartUploadResponse =
        s3Client.createMultipartUpload(createMultipartUploadRequest).get();
    assertSuccess(createMultipartUploadResponse);

    final String uploadId = createMultipartUploadResponse.uploadId();

    List<CompletedPart> completedParts = new ArrayList<>();
    final String partText = "a".repeat(PART_SIZE);

    for (int part = 1; part <= NUM_PARTS; part++) {
      AsyncRequestBody requestBody = AsyncRequestBody.fromString(partText);
      UploadPartRequest uploadPartRequest =
          UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId).partNumber(part).build();
      UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody).get();
      assertSuccess(uploadPartResponse);
      logger.info("S3AsyncClient uploaded part " + part + " of " + NUM_PARTS);
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
    inputStream.close();

    ListObjectsV2Request listObjectsV2Request = ListObjectsV2Request.builder().bucket(bucket).build();
    ListObjectsV2Response listObjectsV2Response = s3Client.listObjectsV2(listObjectsV2Request).get();
    assertSuccess(listObjectsV2Response);
    List<S3Object> s3Objects = listObjectsV2Response.contents()
        .stream()
        .filter(obj -> obj.key().equals(key))
        .toList();
    assertThat(s3Objects).hasSize(1);
    S3Object s3Object = s3Objects.get(0);
    assertThat(s3Object.key()).isEqualTo(key);
    assertThat(s3Object.eTag()).isNotNull();
  }

  static public void validateS3Client(
      S3Client s3Client,
      @Nullable StorageClass storageClass)
      throws Exception {
    logger.info("validate S3Client: storageClass=" + storageClass);
    final String bucket = createNewBucket(s3Client);
    putObjectIntoBucket(s3Client, bucket, storageClass);
    uploadMultipartIntoBucket(s3Client, bucket, storageClass);
  }

  static public void uploadMultipartIntoBucket(S3Client s3Client, String bucket, StorageClass storageClass) {

    final String key = "multipart-key-" + UUID.randomUUID();
    CreateMultipartUploadRequest createMultipartUploadRequest =
        CreateMultipartUploadRequest.builder().bucket(bucket).key(key).build();
    CreateMultipartUploadResponse createMultipartUploadResponse =
        s3Client.createMultipartUpload(createMultipartUploadRequest);
    assertSuccess(createMultipartUploadResponse);

    final String uploadId = createMultipartUploadResponse.uploadId();

    List<CompletedPart> completedParts = new ArrayList<>();
    final String partText = "a".repeat(PART_SIZE);

    for (int part = 1; part <= NUM_PARTS; part++) {
      RequestBody requestBody = RequestBody.fromString(partText);
      UploadPartRequest uploadPartRequest =
          UploadPartRequest.builder().bucket(bucket).key(key).uploadId(uploadId).partNumber(part).build();
      UploadPartResponse uploadPartResponse = s3Client.uploadPart(uploadPartRequest, requestBody);
      assertSuccess(uploadPartResponse);
      logger.info("S3Client uploaded part " + part + " of " + NUM_PARTS);
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
    List<S3Object> s3Objects = listObjectsV2Response.contents()
        .stream()
        .filter(obj -> obj.key().equals(key))
        .toList();
    assertThat(s3Objects).hasSize(1);
    S3Object s3Object = s3Objects.get(0);
    assertThat(s3Object.key()).isEqualTo(key);
    assertThat(s3Object.eTag()).isNotNull();
  }

  private static void putObjectIntoBucket(final S3Client s3Client, final String bucket, @Nullable final StorageClass storageClass) {
    final String key = "putObject-s3Client-key-" + UUID.randomUUID().toString();
    final String data = "Hello-" + UUID.randomUUID().toString();

    PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).storageClass(storageClass).build();
    PutObjectResponse response = s3Client.putObject(request, RequestBody.fromString(data));
    assertSuccess(response);
    assertThat(response.eTag()).isNotNull();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
    assertThat(responseBytes.asUtf8String()).isEqualTo(data);
  }

  private static void putObjectIntoBucket(final S3AsyncClient s3Client, final String bucket, final StorageClass storageClass) throws Exception {
    final String key = "putObject-s3AsyncClient-key-" + UUID.randomUUID().toString();
    final String data = "Hello-" + UUID.randomUUID().toString();

    PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).storageClass(storageClass).build();
    PutObjectResponse response = s3Client.putObject(request, AsyncRequestBody.fromString(data)).get();
    assertSuccess(response);
    assertThat(response.eTag()).isNotNull();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()).get();
    assertThat(responseBytes.asUtf8String()).isEqualTo(data);
  }

  public static void exerciseTransferManager(S3AsyncClient s3Client, @Nullable StorageClass storageClass)
      throws Exception {
    logger.info("exerciseTransferManager: " + s3Client.getClass().getSimpleName());

    final String bucket = createNewBucket(s3Client);
    final String uploadKey = UUID.randomUUID().toString();
    final String payload = "Hello world";

    final LoggingTransferListener listener = LoggingTransferListener.create();

    try (S3TransferManager transferManager = S3TransferManager.builder().s3Client(s3Client).build()) {

      Upload upload = transferManager.upload(
          uploadReq -> uploadReq.requestBody(AsyncRequestBody.fromString(payload)).addTransferListener(listener)
              .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(uploadKey).storageClass(storageClass).build()));
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

  public static String createNewBucket(final S3Client s3Client) {
    final String bucket = BUCKET_PREFIX + UUID.randomUUID();

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucket);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
    assertSuccess(createBucketResponse);
    return bucket;
  }

  public static String createNewBucket(S3AsyncClient s3Client)
      throws ExecutionException, InterruptedException {

    final String bucket = BUCKET_PREFIX + UUID.randomUUID();

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucket);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest).get();
    assertSuccess(createBucketResponse);
    return bucket;
  }

  public static void assertSuccess(final SdkResponse sdkResponse) {
    assertThat(sdkResponse.sdkHttpResponse().isSuccessful()).isTrue();
  }

}