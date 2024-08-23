package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.testcontainers.containers.CephContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Disabled
@Testcontainers
public class S3CephTest extends AbstractS3Test {
  @Container
  private static final CephContainer CEPH_CONTAINER = new CephContainer();

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Ceph(CEPH_CONTAINER));
  }

}
