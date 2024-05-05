package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import java.net.URI;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;


public interface CloudRuntime {
  S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder);
  AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder);

  class Localstack implements CloudRuntime {
    private final LocalStackContainer container;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public Localstack(LocalStackContainer container) {
      if (!container.isRunning()) {
        throw new IllegalStateException("container is not running");
      }
      this.container = container;
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create(container.getAccessKey(), container.getSecretKey())
      );
      this.awsRegion = Region.of(container.getRegion());
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.endpointOverride(container.getEndpoint())
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.endpointOverride(container.getEndpoint())
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }
  }

  class Minio implements CloudRuntime {
    private final MinIOContainer container;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public Minio(MinIOContainer container) {
      if (!container.isRunning()) {
        throw new IllegalStateException("container is not running");
      }
      this.container = container;
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create("minioadmin", "minioadmin")
      );
      this.awsRegion = Region.US_EAST_1;
      this.endpoint = URI.create("http://127.0.0.1:" + container.getFirstMappedPort());
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.endpointOverride(endpoint)
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.endpointOverride(endpoint)
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }
  }

  class S3Mock implements CloudRuntime {
    private final S3MockContainer container;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public S3Mock(S3MockContainer container) {
      this.container = container;
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create("dummy", "dummy")
      );
      this.awsRegion = Region.US_EAST_1;
      this.endpoint = URI.create("http://127.0.0.1:" + this.container.getHttpServerPort());
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.endpointOverride(endpoint)
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.endpointOverride(endpoint)
          .credentialsProvider(awsCredentialsProvider)
          .region(awsRegion);
    }
  }

  class Aws implements CloudRuntime {

    public Aws() { }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder;
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder;
    }
  }

  class Cloudflare implements CloudRuntime {
    private final URI endpointUri;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public Cloudflare(String cloudflareAccountId, String accessKeyId, String secretAccessKey) {
        this.endpointUri = URI.create("https://" + cloudflareAccountId + ".r2.cloudflarestorage.com");
        this.awsCredentialsProvider =
            StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        this.awsRegion = Region.of("auto");
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
        return builder.region(awsRegion).credentialsProvider(awsCredentialsProvider).endpointOverride(endpointUri);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
        return builder.region(awsRegion).credentialsProvider(awsCredentialsProvider).endpointOverride(endpointUri);
    }
  }

  class CloudflareLocal implements CloudRuntime {
    private CloudflareLocalContainer container;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public CloudflareLocal(CloudflareLocalContainer container) {
      if (!container.isRunning()) {
        throw new IllegalStateException("container is not running");
      }
      this.container = container;
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create("dummy", "dummy")
      );
      this.awsRegion = Region.of("auto");
      this.endpoint = URI.create("http://127.0.0.1:" + this.container.getFirstMappedPort());
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.region(this.awsRegion)
          .endpointOverride(this.endpoint)
          .credentialsProvider(this.awsCredentialsProvider);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.region(this.awsRegion)
          .endpointOverride(this.endpoint)
          .credentialsProvider(this.awsCredentialsProvider);
    }
  }
}