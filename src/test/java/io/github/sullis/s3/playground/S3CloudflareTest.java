package io.github.sullis.s3.playground;

import org.junit.jupiter.api.Disabled;

@Disabled
public class S3CloudflareTest extends AbstractS3Test {

  @Override
  protected ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Cloudflare("aaa", "bbb", "ccc");
 }

}
