package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;


// Google Cloud : this file is a work in progress

@Disabled
public class S3GoogleCloudTest extends AbstractS3Test {

  @Override
  protected List<ObjectStorageProvider> objectStorageProviders() {
    return List.of(new ObjectStorageProvider.GoogleCloud("aaa", "bbb"));
 }

}
