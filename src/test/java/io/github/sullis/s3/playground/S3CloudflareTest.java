package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
public class S3CloudflareTest extends AbstractS3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3CloudflareTest.class);

  @Override
  protected List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Cloudflare("aaa", "bbb", "ccc"));
 }

}
