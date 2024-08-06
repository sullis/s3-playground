package io.github.sullis.s3.playground.testkit;

import org.jspecify.annotations.Nullable;
import software.amazon.awssdk.services.s3.model.StorageClass;


public interface S3TestKit {
  String createNewBucket() throws Exception;
  void deleteBucket(String bucketName) throws Exception;
  void validate(@Nullable StorageClass storageClass) throws Exception;
  void uploadMultiPartIntoBucket(String bucket, @Nullable StorageClass storageClass) throws Exception;
  void putObjectIntoBucket(String bucket, StorageClass storageClass) throws Exception;
  void cleanup() throws Exception;
}
