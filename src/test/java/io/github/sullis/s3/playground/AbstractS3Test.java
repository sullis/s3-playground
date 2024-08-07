package io.github.sullis.s3.playground;

import io.github.sullis.s3.playground.testkit.S3AsyncTestKit;
import io.github.sullis.s3.playground.testkit.S3SyncTestKit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3CrtAsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.StorageClass;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractS3Test {

  private static final List<SdkAsyncHttpClient.Builder<?>> ASYNC_HTTP_CLIENT_BUILDER_LIST =
      List.of(NettyNioAsyncHttpClient.builder(), AwsCrtAsyncHttpClient.builder());

  private static final List<SdkHttpClient.Builder<?>> SYNC_HTTP_CLIENT_BUILDER_LIST =
      List.of(ApacheHttpClient.builder(), AwsCrtHttpClient.builder());

  private final Logger logger = LoggerFactory.getLogger(getClass());

  protected abstract List<ObjectStorageProvider> objectStorageProviders();

  public StorageClass[] storageClasses() {
    return new StorageClass[] {
        StorageClass.STANDARD,
    };
  }

  public List<S3AsyncClientInfo> s3AsyncClients() {
    List<S3AsyncClientInfo> result = new ArrayList<>();
    for (ObjectStorageProvider objectStorage : objectStorageProviders()) {
      ASYNC_HTTP_CLIENT_BUILDER_LIST.forEach(httpClientBuilder -> {
        var httpClient = httpClientBuilder.build();
        S3AsyncClient s3Client =
            (S3AsyncClient) objectStorage.configure(S3AsyncClient.builder().httpClient(httpClient)).build();
        result.add(new S3AsyncClientInfo(httpClient.clientName(), objectStorage, s3Client));
      });

      // S3 crtBuilder
      S3CrtAsyncClientBuilder crtBuilder =
          S3AsyncClient.crtBuilder().checksumValidationEnabled(true).maxConcurrency(3).targetThroughputInGbps(0.5)
              .minimumPartSizeInBytes(1_000_000L);
      result.add(new S3AsyncClientInfo("crtBuilder", objectStorage, objectStorage.configure(crtBuilder).build()));
    }

    return result;
  }

  public List<S3ClientInfo> s3Clients() {
    List<S3ClientInfo> result = new ArrayList<>();
    for (ObjectStorageProvider objectStorageProvider : objectStorageProviders()) {
      SYNC_HTTP_CLIENT_BUILDER_LIST.forEach(httpClientBuilder -> {
        var httpClient = httpClientBuilder.build();
        S3Client s3Client =
            (S3Client) objectStorageProvider.configure(S3Client.builder().httpClient(httpClient)).build();
        result.add(new S3ClientInfo(httpClient.clientName(), objectStorageProvider, s3Client));
      });
    }

    return result;
  }

  private Stream<Arguments> s3AsyncClientArguments() {
    List<Arguments> argumentsList = new ArrayList<>();
    for (StorageClass storageClass : storageClasses()) {
      for (S3AsyncClientInfo s3AsyncClient : s3AsyncClients()) {
        argumentsList.add(Arguments.of(s3AsyncClient, storageClass));
      }
    }
    return argumentsList.stream();
  }

  private Stream<Arguments> s3ClientArguments() {
    List<Arguments> argumentsList = new ArrayList<>();
    for (StorageClass storageClass : storageClasses()) {
      for (S3ClientInfo s3Client : s3Clients()) {
        argumentsList.add(Arguments.of(s3Client, storageClass));
      }
    }
    return argumentsList.stream();
  }

  @ParameterizedTest
  @MethodSource("s3AsyncClientArguments")
  public void validateS3AsyncClient(S3AsyncClientInfo s3ClientInfo, StorageClass storageClass)
      throws Exception {
    S3AsyncTestKit testkit = new S3AsyncTestKit(s3ClientInfo.client);
    try {
      testkit.validate(storageClass);
    } finally {
      testkit.cleanup();
    }
  }

  @ParameterizedTest
  @MethodSource("s3ClientArguments")
  public void validateS3Client(S3ClientInfo s3ClientInfo, StorageClass storageClass)
      throws Exception {
    S3AsyncClient asyncClient = S3ClientAdapter.adapt(s3ClientInfo.client);
    S3AsyncTestKit testkit = new S3AsyncTestKit(asyncClient);
    try {
      testkit.validate(storageClass);
    } finally {
      testkit.cleanup();
    }
  }

  public record S3AsyncClientInfo(String httpClientDescription,
                                  ObjectStorageProvider objectStorageProvider,
                                  S3AsyncClient client) {
    @Override
    public String toString() {
      return objectStorageProvider.getClass().getSimpleName() + ":" + httpClientDescription + ":"
          + this.client.getClass().getSimpleName();
    }
  }

  public record S3ClientInfo(String httpClientDescription,
                             ObjectStorageProvider objectStorageProvider,
                             S3Client client) {
    @Override
    public String toString() {
      return objectStorageProvider.getClass().getSimpleName() + ":" + httpClientDescription + ":"
          + this.client.getClass().getSimpleName();
    }
  }
}