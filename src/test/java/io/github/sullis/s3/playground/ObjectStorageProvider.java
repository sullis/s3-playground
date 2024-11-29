package io.github.sullis.s3.playground;

import com.adobe.testing.s3mock.testcontainers.S3MockContainer;
import java.net.URI;
import org.testcontainers.containers.CephContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;


public interface ObjectStorageProvider {
  S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder);
  AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder);
  default boolean isLocal() { return false; }
  default boolean supportsBucketExpiration() { return true; }
  default boolean supportsConditionalWrites() { return true; }


  class Localstack implements ObjectStorageProvider {
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
    public boolean isLocal() {
      return true;
    }

    @Override
    public boolean supportsBucketExpiration() {
      return false;
    }

    @Override
    public boolean supportsConditionalWrites() { return false; }

    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }
  }

  class Minio implements ObjectStorageProvider {
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public Minio(MinIOContainer container) {
      if (!container.isRunning()) {
        throw new IllegalStateException("container is not running");
      }
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
    public boolean isLocal() {
      return true;
    }

    @Override
    public boolean supportsBucketExpiration() {
      return false;
    }

    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }
  }

  class S3Mock implements ObjectStorageProvider {
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public S3Mock(S3MockContainer container) {
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create("dummy", "dummy")
      );
      this.awsRegion = Region.US_EAST_1;
      this.endpoint = URI.create("http://127.0.0.1:" + container.getHttpServerPort());
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
    public boolean isLocal() {
      return true;
    }
  }

  class Ceph implements ObjectStorageProvider {
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;
    private final URI endpoint;

    public Ceph(final CephContainer container) {
      this.awsCredentialsProvider = StaticCredentialsProvider.create(
          AwsBasicCredentials.create(container.getCephAccessKey(), container.getCephSecretKey()));
      this.awsRegion = Region.US_EAST_1;
      this.endpoint = URI.create("http://127.0.0.1:" + container.getCephPort());
    }

    @Override
    public boolean isLocal() {
      return true;
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

  class Aws implements ObjectStorageProvider {

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

  class Tigris implements ObjectStorageProvider {
    private final URI endpointUri;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public Tigris(String accessKeyId, String secretAccessKey) {
      this.endpointUri = URI.create("https://fly.storage.tigris.dev");
      this.awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
      this.awsRegion = Region.of("auto");
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }
  }

  /*

     Google Cloud with S3
     https://cloud.google.com/storage/docs/aws-simple-migration

   */
  class GoogleCloud implements ObjectStorageProvider {
    private final URI endpointUri;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public GoogleCloud(String googleAccessKeyId, String googleSecretAccessKey) {
      this.endpointUri = URI.create("https://storage.googleapis.com");
      this.awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(googleAccessKeyId, googleSecretAccessKey));
      this.awsRegion = Region.of("auto");
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }
  }

  /*

     Cloudflare R2 storage
     https://developers.cloudflare.com/r2/

   */
  class Cloudflare implements ObjectStorageProvider {
    private final URI endpointUri;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public Cloudflare(String cloudflareAccountId, String accessKeyId, String secretAccessKey) {
        this.endpointUri = URI.create("https://" + cloudflareAccountId + ".r2.cloudflarestorage.com");
        this.awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        this.awsRegion = Region.of("auto");
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public boolean supportsBucketExpiration() { return false; }
  }

  /*

     Wasabi S3 Object Storage

     https://docs.wasabi.com/docs/wasabi-api

     https://docs.wasabi.com/docs/how-do-i-use-aws-sdk-for-java-v2-with-wasabi

   */
  class Wasabi implements ObjectStorageProvider {
    private final URI endpointUri;
    private final AwsCredentialsProvider awsCredentialsProvider;
    private final Region awsRegion;

    public Wasabi(AwsCredentialsProvider credentialsProvider) {
      this.endpointUri = URI.create("https://s3.wasabisys.com/");
      this.awsCredentialsProvider = credentialsProvider;
      this.awsRegion = Region.of("us-east-1");
    }

    @Override
    public S3CrtAsyncClientBuilder configure(S3CrtAsyncClientBuilder builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public AwsClientBuilder<?, ?> configure(AwsClientBuilder<?, ?> builder) {
      return builder.region(awsRegion)
          .credentialsProvider(awsCredentialsProvider)
          .endpointOverride(endpointUri);
    }

    @Override
    public boolean supportsConditionalWrites() { return false; }
  }

}