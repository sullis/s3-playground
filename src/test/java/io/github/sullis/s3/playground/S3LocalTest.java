package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.CephContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.utility.DockerImageName;


public class S3LocalTest extends AbstractS3Test {
  private static final MinIOContainer MINIO_CONTAINER = new MinIOContainer(DockerImageName.parse("minio/minio:latest"));

  private static final S3MockContainer S3_MOCK_CONTAINER = new S3MockContainer(DockerImageName.parse("adobe/s3mock:latest"));

  private static final CephContainer CEPH_CONTAINER = new CephContainer();


  @BeforeAll
  public static void startContainers() {
    MINIO_CONTAINER.start();
    S3_MOCK_CONTAINER.start();
    CEPH_CONTAINER.start();
  }

  @AfterAll
  public static void stopContainers() {
    if (MINIO_CONTAINER != null) {
      MINIO_CONTAINER.stop();
    }
    if (S3_MOCK_CONTAINER != null) {
      S3_MOCK_CONTAINER.stop();
    }
    if (CEPH_CONTAINER != null) {
      CEPH_CONTAINER.stop();
    }
  }

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(
        new ObjectStorageProvider.Minio(MINIO_CONTAINER),
        new ObjectStorageProvider.S3Mock(S3_MOCK_CONTAINER));
        // TODO new ObjectStorageProvider.Ceph(CEPH_CONTAINER));
  }

}
