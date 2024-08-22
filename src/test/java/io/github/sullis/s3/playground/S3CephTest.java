package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.testcontainers.containers.CephContainer;

@Disabled
public class S3CephTest extends AbstractS3Test {
  private static final CephContainer CEPH_CONTAINER = new CephContainer();

  @BeforeAll
  public static void startContainers() {
    CEPH_CONTAINER.start();
  }

  @AfterAll
  public static void stopContainers() {
    if (CEPH_CONTAINER != null) {
      CEPH_CONTAINER.stop();
    }
  }

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Ceph(CEPH_CONTAINER));
  }

}
