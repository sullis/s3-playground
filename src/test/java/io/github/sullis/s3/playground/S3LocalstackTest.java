package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;


public class S3LocalstackTest extends AbstractS3Test {
  private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:s3-latest"))
      .withServices(LocalStackContainer.Service.S3);

  @BeforeAll
  public static void startContainers() {
    LOCALSTACK.start();
  }

  @AfterAll
  public static void stopContainers() {
    if (LOCALSTACK != null) {
      LOCALSTACK.stop();
    }
  }

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Localstack(LOCALSTACK));
  }

}
