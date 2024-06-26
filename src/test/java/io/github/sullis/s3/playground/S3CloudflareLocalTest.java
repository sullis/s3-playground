package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;


// Cloudflare Local is a work-in-progress
@Disabled
public class S3CloudflareLocalTest extends AbstractS3Test {
  private static final CloudflareLocalContainer CLOUDFLARE_LOCAL = CloudflareLocalContainer.create();

  @BeforeAll
  public static void startContainers() {
    CLOUDFLARE_LOCAL.start();
    assertThat(CLOUDFLARE_LOCAL.isRunning()).isTrue();
  }

  @AfterAll
  public static void stopContainers() {
    if (CLOUDFLARE_LOCAL != null) {
      CLOUDFLARE_LOCAL.stop();
    }
  }

  @Override
  public List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(
        new ObjectStorageProvider.CloudflareLocal(CLOUDFLARE_LOCAL));
  }

}
