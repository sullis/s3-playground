package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;


/**   Adobe S3Mock   */
@Testcontainers
public class S3MockTest extends AbstractS3Test {
  @Container
  private static final S3MockContainer S3_MOCK_CONTAINER = new S3MockContainer(DockerImageName.parse("adobe/s3mock:latest"));

  @Override
  public ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.S3Mock(S3_MOCK_CONTAINER);
  }

}
