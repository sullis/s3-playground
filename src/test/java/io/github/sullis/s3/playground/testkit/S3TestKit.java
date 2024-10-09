package io.github.sullis.s3.playground.testkit;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter;
import software.amazon.awssdk.services.s3.model.StorageClass;


public interface S3TestKit {
  String BUCKET_PREFIX = "test-bucket-";

  default String generateUniqueBucketName() {
    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
    String day = format.format(new java.util.Date());
    return BUCKET_PREFIX + day + "-" + UUID.randomUUID();
  }

  void validate(@Nullable StorageClass storageClass) throws Exception;
  String createNewBucket() throws Exception;
  void deleteBucket(String bucketName) throws Exception;
  void uploadMultiPartIntoBucket(String bucket, @Nullable StorageClass storageClass) throws Exception;
  void putObjectIntoBucket(String bucket, StorageClass storageClass) throws Exception;
  void cleanup() throws Exception;

  default @Nullable BucketLifecycleConfiguration createBucketExpiration(final int days) {
    if (days < 1) {
      return null;
    }

    LifecycleExpiration expiration = LifecycleExpiration.builder()
        .days(days)
        .build();

    LifecycleRuleFilter filter = LifecycleRuleFilter.builder().prefix("").build();

    LifecycleRule rule = LifecycleRule.builder()
        .expiration(expiration)
        .status(ExpirationStatus.ENABLED)
        .filter(filter)
        .build();
    return BucketLifecycleConfiguration.builder()
        .rules(rule)
        .build();
  }
}
