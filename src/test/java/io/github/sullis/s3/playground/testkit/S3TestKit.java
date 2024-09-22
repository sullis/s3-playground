package io.github.sullis.s3.playground.testkit;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.StorageClass;


public interface S3TestKit {
  void validate(@Nullable StorageClass storageClass) throws Exception;
  String createNewBucket() throws Exception;
  void deleteBucket(String bucketName) throws Exception;
  void uploadMultiPartIntoBucket(String bucket, @Nullable StorageClass storageClass) throws Exception;
  void putObjectIntoBucket(String bucket, StorageClass storageClass) throws Exception;
  void cleanup() throws Exception;

  default @Nullable BucketLifecycleConfiguration createBucketExpiration(final Duration duration) {
    if (duration.toSeconds() < 1) {
      return null;
    }

    Instant expirationDate = Instant.now().minus(duration);

    LifecycleExpiration expiration = LifecycleExpiration.builder()
        .date(expirationDate)
        .build();
    LifecycleRule rule = LifecycleRule.builder()
        .expiration(expiration)
        .status(ExpirationStatus.ENABLED)
        .build();
    return BucketLifecycleConfiguration.builder()
        .rules(rule)
        .build();
  }
}
