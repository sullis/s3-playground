package io.github.sullis.s3.playground;

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
  public ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Ceph(CEPH_CONTAINER);
  }

}
