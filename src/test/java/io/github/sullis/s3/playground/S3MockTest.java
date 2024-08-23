package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import java.util.List;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


@Testcontainers
public class S3MockTest extends AbstractS3Test {
  @Container
  private static final S3MockContainer S3_MOCK_CONTAINER = new S3MockContainer(DockerImageName.parse("adobe/s3mock:latest"));

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.S3Mock(S3_MOCK_CONTAINER));
  }

}
