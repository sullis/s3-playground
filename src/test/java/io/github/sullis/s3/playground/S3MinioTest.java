package io.github.sullis.s3.playground;

import java.util.List;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class S3MinioTest extends AbstractS3Test {
  @Container
  private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Minio(MINIO_CONTAINER));
  }

}
