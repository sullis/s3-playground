package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *   Tigris S3 https://www.tigrisdata.com
 *
 */
@Disabled
public class S3TigrisTest extends AbstractS3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3TigrisTest.class);

  @Override
  protected List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.Tigris("aaa", "bbb"));
 }

}
