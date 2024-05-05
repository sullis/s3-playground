package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.model.DataRedundancy;

@Disabled
public class S3CloudflareTest extends AbstractS3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3CloudflareTest.class);

  @Override
  protected List<CloudRuntime> s3Runtimes() {
    return List.of(new CloudRuntime.Cloudflare("aaa", "bbb", "ccc"));
 }

  @Override
  protected DataRedundancy[] dataRedundancyValues() {
    return new DataRedundancy[] { null };
  }
}
