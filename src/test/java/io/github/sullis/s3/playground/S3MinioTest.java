package io.github.sullis.s3.playground;

import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class S3MinioTest extends AbstractS3Test {
  @Container
  private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

  @Override
  public ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Minio(MINIO_CONTAINER);
  }

}
