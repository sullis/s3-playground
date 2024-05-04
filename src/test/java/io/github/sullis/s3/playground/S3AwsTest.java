package io.github.sullis.s3.playground;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.model.DataRedundancy;

import static org.assertj.core.api.Assertions.assertThat;


@Disabled
public class S3AwsTest extends AbstractS3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(S3AwsTest.class);

  @BeforeAll
  public void checkAwsCredentials() {
      try (DefaultCredentialsProvider provider = DefaultCredentialsProvider.create()) {
        AwsCredentials credentials = provider.resolveCredentials();
        assertThat(credentials).isNotNull();
        LOGGER.info("resolved AWS credentials");
      }
  }

  @Override
  protected List<CloudRuntime> s3Runtimes() {
    return List.of(new CloudRuntime.Aws());
 }

  @Override
  protected DataRedundancy[] dataRedundancyValues() {
    return new DataRedundancy[] { null, DataRedundancy.SINGLE_AVAILABILITY_ZONE };
  }
}
