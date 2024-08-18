package io.github.sullis.s3.playground.testkit;

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
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationResponse;
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


public class S3AsyncTestKit implements S3TestKit {
  private static final String BUCKET_PREFIX = "test-bucket-";
  private static final int PART_SIZE = 5 * 1024 * 1024;
  private static final int NUM_PARTS = 3;
  private static final long EXPECTED_OBJECT_SIZE = NUM_PARTS * PART_SIZE;

  private static final Logger logger = LoggerFactory.getLogger(S3AsyncTestKit.class);

  private List<String> bucketsCreated = new ArrayList<>();
  private final S3AsyncClient s3Client;

  public S3AsyncTestKit(final S3AsyncClient s3Client) {
    this.s3Client = s3Client;
  }

  public void validate(@Nullable StorageClass storageClass)
      throws Exception {
    logger.info("validate S3AsyncClient: storageClass=" + storageClass);
    final String bucket = createNewBucket();
    putObjectIntoBucket(bucket, storageClass);
    uploadMultiPartIntoBucket(bucket, storageClass);
    exerciseTransferManager(storageClass);
  }

  @Override
  public void uploadMultiPartIntoBucket(String bucket, @Nullable StorageClass storageClass) throws Exception {

    final String contentType = "plain/text";
    final String key = "multipart-key-" + UUID.randomUUID();
    CreateMultipartUploadRequest createMultipartUploadRequest =
        CreateMultipartUploadRequest.builder().bucket(bucket).key(key).storageClass(storageClass).contentType(contentType).build();
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
    assertThat(completeMultipartUploadResponse.location())
        .contains(bucket)
        .endsWith("/" + key);

    assertKeyExists(bucket, key);

    Path localPath = Path.of(Files.temporaryFolderPath() + "/" + UUID.randomUUID().toString());
    File localFile = localPath.toFile();
    localFile.deleteOnExit();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    GetObjectResponse getObjectResponse = s3Client.getObject(getObjectRequest, localFile.toPath()).get();
    assertSuccess(getObjectResponse);
    assertThat(getObjectResponse.contentType()).startsWith("plain/text");
    assertThat(getObjectResponse.eTag()).isNotNull();

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
    assertThat(s3Object.size()).isEqualTo(EXPECTED_OBJECT_SIZE);
  }

  @Override
  public void putObjectIntoBucket(final String bucket, final StorageClass storageClass) throws Exception {
    final String key = "putObject-key-" + UUID.randomUUID().toString();
    final String data = "Hello-" + UUID.randomUUID().toString();

    PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(key).storageClass(storageClass).build();
    PutObjectResponse response = s3Client.putObject(request, AsyncRequestBody.fromString(data)).get();
    assertSuccess(response);
    assertThat(response.eTag()).isNotNull();

    assertKeyExists(bucket, key);

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()).get();
    assertThat(responseBytes.asUtf8String()).isEqualTo(data);
  }

  public void exerciseTransferManager(@Nullable StorageClass storageClass)
      throws Exception {
    logger.info("exerciseTransferManager: " + s3Client.getClass().getSimpleName());

    final String bucket = createNewBucket();
    final String uploadKey = "upload-key-" + UUID.randomUUID().toString();
    final String payload = "Hello world";

    final LoggingTransferListener listener = LoggingTransferListener.create();

    try (S3TransferManager transferManager = S3TransferManager.builder().s3Client(s3Client).build()) {

      Upload upload = transferManager.upload(
          uploadReq -> uploadReq.requestBody(AsyncRequestBody.fromString(payload)).addTransferListener(listener)
              .putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(uploadKey).storageClass(storageClass).build()));
      CompletedUpload completedUpload = upload.completionFuture().get();
      assertThat(completedUpload.response().eTag()).isNotNull();
      assertThat(completedUpload.response().expiration()).isNull();

      PutObjectResponse putObjectResponse = completedUpload.response();
      assertSuccess(putObjectResponse);
      assertThat(putObjectResponse.eTag()).isNotNull();
      assertThat(putObjectResponse.expiration()).isNull();

      assertKeyExists(bucket, uploadKey);

      File destinationFile = Files.newTemporaryFile();

      DownloadFileRequest downloadFileRequest =
          DownloadFileRequest.builder().addTransferListener(listener).destination(destinationFile)
              .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(uploadKey).build()).build();

      FileDownload fileDownload = transferManager.downloadFile(downloadFileRequest);
      CompletedFileDownload completedFileDownload = fileDownload.completionFuture().get();

      GetObjectResponse getObjectResponse = completedFileDownload.response();
      assertSuccess(getObjectResponse);
      assertThat(getObjectResponse.eTag()).isNotNull();
      assertThat(getObjectResponse.cacheControl()).isNull();
      assertThat(getObjectResponse.contentType()).startsWith("text/plain;");

      assertThat(destinationFile).isFile();
      assertThat(destinationFile).hasSize(11);

      assertThat(destinationFile).content()
          .isEqualTo(payload);
    }
  }

  public void assertBucketExists(final String bucketName)
      throws ExecutionException, InterruptedException {
    Bucket bucket = s3Client.listBuckets().get().buckets().stream().filter(b -> b.name().equals(bucketName)).findFirst().get();
    assertThat(bucket.creationDate()).isNotNull();
    assertThat(bucket.name()).isEqualTo(bucketName);

    HeadBucketResponse headBucketResponse = s3Client.headBucket(request -> request.bucket(bucketName)).get();
    assertSuccess(headBucketResponse);
  }

  public void assertKeyExists(final String bucketName, final String key) throws Exception {
    HeadObjectResponse headBucketResponse = s3Client.headObject(request -> request.bucket(bucketName).key(key)).get();
    assertSuccess(headBucketResponse);
    assertThat(headBucketResponse.eTag()).isNotNull();
    assertThat(headBucketResponse.contentLength()).isGreaterThan(0);
  }

  public String createNewBucket()
      throws ExecutionException, InterruptedException {

    final String bucketName = BUCKET_PREFIX + UUID.randomUUID();
    bucketsCreated.add(bucketName);

    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucketName);
    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest).get();
    assertSuccess(createBucketResponse);

    LifecycleRule expiration = LifecycleRule.builder()
        .status(ExpirationStatus.ENABLED)
        .expiration(LifecycleExpiration.builder().days(1).build())
        .build();

    PutBucketLifecycleConfigurationResponse lifecycleConfigurationResponse = s3Client.putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest.builder()
        .bucket(bucketName)
        .lifecycleConfiguration(config ->
            config.rules(expiration)).build()).get();

    assertSuccess(lifecycleConfigurationResponse);

    assertBucketExists(bucketName);

    return bucketName;
  }

  private static void assertSuccess(final SdkResponse sdkResponse) {
    assertThat(sdkResponse.sdkHttpResponse().isSuccessful()).isTrue();
  }

  public void deleteBucket(final String bucketName)
      throws ExecutionException, InterruptedException {
    logger.info("deleteBucket: " + bucketName);
    ListObjectsV2Response listResponse = s3Client.listObjectsV2(request -> request.bucket(bucketName)).get();
    for (S3Object s3Object : listResponse.contents()) {
      s3Client.deleteObject(request -> request.bucket(bucketName).key(s3Object.key())).get();
    }
    DeleteBucketResponse response = s3Client.deleteBucket(request -> request.bucket(bucketName)).get();
    assertSuccess(response);
  }

  public void cleanup() {
    for (String bucketName : bucketsCreated) {
      try {
        deleteBucket(bucketName);
      } catch (Exception ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}