package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
public class S3CloudflareTest extends AbstractS3Test {

  @Override
  protected ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Cloudflare("aaa", "bbb", "ccc");
 }

}
