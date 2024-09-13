package io.github.sullis.s3.playground.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.metrics.SdkMetric;

public class MicrometerPublisher
    implements MetricPublisher {
  // visible for testing
  public final MeterRegistry registry;

  public MicrometerPublisher(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void publish(MetricCollection metricCollection) {
    // TODO : WIP
    String name = metricCollection.name();
    System.out.println("publish called. name=" + name);
    metricCollection.stream().map(metricRecord -> {
      System.out.println("metricRecord: " + metricRecord);
      SdkMetric sdkMetric = metricRecord.metric();
      System.out.println("name=" + name
          + " sdkMetric="
          + sdkMetric
          + " level="
          + sdkMetric.level());
      return metricRecord;
    }).toList();
  }

  @Override
  public void close() {
    registry.close();
  }
}
