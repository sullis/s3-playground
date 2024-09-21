package io.github.sullis.s3.playground;

import java.util.List;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class S3LocalstackTest extends AbstractS3Test {
  @Container
  private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(DockerImageName.parse("localstack/localstack:s3-latest"))
      .withServices(LocalStackContainer.Service.S3);

  @Override
  public ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Localstack(LOCALSTACK);
  }

}
