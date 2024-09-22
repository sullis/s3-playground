package io.github.sullis.s3.playground;

import org.junit.jupiter.api.Disabled;


/**
 *
 *   Tigris S3 https://www.tigrisdata.com
 *
 */
@Disabled
public class S3TigrisTest extends AbstractS3Test {

  @Override
  protected ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Tigris("aaa", "bbb");
 }

}
