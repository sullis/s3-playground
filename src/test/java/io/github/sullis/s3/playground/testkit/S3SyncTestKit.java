package io.github.sullis.s3.playground.testkit;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Files;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.StorageClass;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


public class S3SyncTestKit implements S3TestKit {
  private static final int PART_SIZE = 5 * 1024 * 1024;
  private static final int NUM_PARTS = 3;
  private static final long EXPECTED_OBJECT_SIZE = NUM_PARTS * PART_SIZE;

  private static final Logger logger = LoggerFactory.getLogger(S3SyncTestKit.class);

  private List<String> bucketsCreated = new ArrayList<>();
  private final S3Client s3Client;
  private final int bucketExpirationDays;
  private final boolean exerciseConditionalWrites;

  public S3SyncTestKit(S3Client s3Client, int bucketExpirationDays, boolean exerciseConditionalWrites) {
    this.s3Client = s3Client;
    this.bucketExpirationDays = bucketExpirationDays;
    this.exerciseConditionalWrites = exerciseConditionalWrites;
  }

  public void validate(@Nullable StorageClass storageClass)
      throws Exception {
    logger.info("validate S3Client: storageClass={}", storageClass);
    final String bucket = createNewBucket();
    putObjectIntoBucket(bucket, storageClass);
    uploadMultiPartIntoBucket(bucket, storageClass);
    if (exerciseConditionalWrites) {
      exerciseConditionalWrite(bucket);
    }
  }

  @Override
  public void uploadMultiPartIntoBucket(String bucket, @Nullable StorageClass storageClass) throws Exception {

    final String key = "multipart-key-" + UUID.randomUUID();
    CreateMultipartUploadRequest.Builder createMultipartUploadBuilder = CreateMultipartUploadRequest.builder().bucket(bucket).key(key);
    if (storageClass != null) {
      createMultipartUploadBuilder.storageClass(storageClass);
    }
    CreateMultipartUploadRequest createMultipartUploadRequest = createMultipartUploadBuilder.build();
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
      logger.info("S3Client uploaded part {} of {}", part, NUM_PARTS);
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
    assertThat(completeMultipartUploadResponse.location())
        .contains(bucket)
        .endsWith("/" + key);

    assertKeyExists(bucket, key);

    Path localPath = Path.of(Files.temporaryFolderPath() + "/" + UUID.randomUUID().toString());
    File localFile = localPath.toFile();
    localFile.deleteOnExit();

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    GetObjectResponse getObjectResponse = s3Client.getObject(getObjectRequest, localFile.toPath());
    assertSuccess(getObjectResponse);
    assertThat(getObjectResponse.contentType()).isNotNull();

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
    assertThat(s3Object.size()).isEqualTo(EXPECTED_OBJECT_SIZE);
  }

  @Override
  public void putObjectIntoBucket(final String bucket, @Nullable final StorageClass storageClass) throws Exception {
    final String key = "putObject-s3Client-key-" + UUID.randomUUID().toString();
    final String data = "Hello-" + UUID.randomUUID().toString();

    PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder().bucket(bucket).key(key);
    if (storageClass != null) {
      requestBuilder.storageClass(storageClass);
    }
    PutObjectRequest request = requestBuilder.build();
    PutObjectResponse response = s3Client.putObject(request, RequestBody.fromString(data));
    assertSuccess(response);
    assertThat(response.eTag()).isNotNull();

    assertKeyExists(bucket, key);

    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();
    ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObject(getObjectRequest, ResponseTransformer.toBytes());
    assertThat(responseBytes.asUtf8String()).isEqualTo(data);
  }


  @Override
  public String createNewBucket() throws Exception {
    final String bucketName = generateUniqueBucketName();
    bucketsCreated.add(bucketName);


    CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder().bucket(bucketName);



    CreateBucketRequest createBucketRequest = createBucketRequestBuilder.build();
    CreateBucketResponse createBucketResponse = s3Client.createBucket(createBucketRequest);
    assertSuccess(createBucketResponse);

    assertBucketExists(bucketName);

    BucketLifecycleConfiguration blConfig = createBucketExpiration(this.bucketExpirationDays);
    if (blConfig != null) {
      PutBucketLifecycleConfigurationResponse
          response = s3Client.putBucketLifecycleConfiguration(
          PutBucketLifecycleConfigurationRequest.builder()
              .bucket(bucketName)
              .lifecycleConfiguration(blConfig)
              .build());
      assertSuccess(response);
    }

    return bucketName;
  }

  @Override
  public void deleteBucket(final String bucketName) throws Exception {
    logger.info("deleteBucket: {}", bucketName);
    for (S3Object s3Object : s3Client.listObjectsV2(request -> request.bucket(bucketName)).contents()) {
      s3Client.deleteObject(request -> request.bucket(bucketName).key(s3Object.key()));
    }
    DeleteBucketResponse response = s3Client.deleteBucket(request -> request.bucket(bucketName));
    assertSuccess(response);
  }

  public void exerciseConditionalWrite(final String bucket) throws Exception {
    logger.info("exerciseConditionalWrite: {}", s3Client.getClass().getSimpleName());

    /* August 2024:

       https://aws.amazon.com/about-aws/whats-new/2024/08/amazon-s3-conditional-writes/

     */

    final String key = "test-key-" + UUID.randomUUID().toString();
    for (int i = 0; i < 5; i++) {
      final String payload = "test-payload-" + i;
      PutObjectRequest request = PutObjectRequest.builder()
          .bucket(bucket)
          .key(key)
          .ifNoneMatch("*")
          .build();
      RequestBody body = RequestBody.fromString(payload);
      if (i == 0) {
        PutObjectResponse response = s3Client.putObject(request, body);
        assertSuccess(response);
      } else {
        assertThatThrownBy(() -> {
          s3Client.putObject(request, body);
        }).isInstanceOf(S3Exception.class)
            .hasMessageContaining("Service: S3, Status Code: 412")
            .hasMessageContaining("At least one of the pre-conditions you specified did not hold");
      }
    }

    var getResponse = s3Client.getObject(
        request -> request.bucket(bucket).key(key),
        ResponseTransformer.toBytes());
    String responseBody = getResponse.asUtf8String();
    assertThat(responseBody).isEqualTo("test-payload-0");
  }

  public void assertBucketExists(final String bucketName) {
    Bucket bucket = s3Client.listBuckets().buckets().stream().filter(b -> b.name().equals(bucketName)).findFirst().get();
    assertThat(bucket.creationDate()).isNotNull();
    assertThat(bucket.name()).isEqualTo(bucketName);

    HeadBucketResponse headBucketResponse = s3Client.headBucket(request -> request.bucket(bucketName));
    assertSuccess(headBucketResponse);
  }

  public void assertKeyExists(final String bucketName, final String key) {
    HeadObjectResponse headBucketResponse = s3Client.headObject(request -> request.bucket(bucketName).key(key));
    assertSuccess(headBucketResponse);
    assertThat(headBucketResponse.eTag()).isNotNull();
    assertThat(headBucketResponse.contentLength()).isGreaterThan(0);
  }

  private static void assertSuccess(final SdkResponse sdkResponse) {
    assertThat(sdkResponse.sdkHttpResponse().isSuccessful()).isTrue();
  }

  @Override
  public void cleanup() throws Exception {
    for (String bucketName : bucketsCreated) {
       deleteBucket(bucketName);
    }
  }
}