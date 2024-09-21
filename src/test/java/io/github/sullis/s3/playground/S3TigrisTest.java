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

  @Override
  protected ObjectStorageProvider objectStorageProvider() {
    return new ObjectStorageProvider.Tigris("aaa", "bbb");
 }

}
