package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;


public class S3MinioTest extends AbstractS3Test {
  private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

  @BeforeAll
  public static void startContainers() {
    MINIO_CONTAINER.start();
  }

  @AfterAll
  public static void stopContainers() {
    if (MINIO_CONTAINER != null) {
      MINIO_CONTAINER.stop();
    }
  }

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Minio(MINIO_CONTAINER));
  }

}
