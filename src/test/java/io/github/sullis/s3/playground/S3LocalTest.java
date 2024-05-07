package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;


public class S3LocalTest extends AbstractS3Test {
  private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:s3-latest"))
      .withServices(LocalStackContainer.Service.S3);

  private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

  private static final S3MockContainer S3_MOCK_CONTAINER = new S3MockContainer(DockerImageName.parse("adobe/s3mock:latest"));


  @BeforeAll
  public static void startContainers() {
    LOCALSTACK.start();
    MINIO_CONTAINER.start();
    S3_MOCK_CONTAINER.start();
  }

  @AfterAll
  public static void stopContainers() {
    if (LOCALSTACK != null) {
      LOCALSTACK.stop();
    }
    if (MINIO_CONTAINER != null) {
      MINIO_CONTAINER.stop();
    }
    if (S3_MOCK_CONTAINER != null) {
      S3_MOCK_CONTAINER.stop();
    }
  }

  @Override
  public List<CloudRuntime> s3Runtimes() {
    return List.of(
        new CloudRuntime.Localstack(LOCALSTACK),
        new CloudRuntime.Minio(MINIO_CONTAINER),
        new CloudRuntime.S3Mock(S3_MOCK_CONTAINER));
  }

}
