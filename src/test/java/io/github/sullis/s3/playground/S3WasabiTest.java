package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;


@Disabled
public class S3WasabiTest extends AbstractS3Test {

  @Override
  protected ObjectStorageProvider objectStorageProvider() {
    AwsCredentials credentials = AwsBasicCredentials.create("aaa", "bbb");
    return new ObjectStorageProvider.Wasabi(StaticCredentialsProvider.create(credentials));
 }

}
