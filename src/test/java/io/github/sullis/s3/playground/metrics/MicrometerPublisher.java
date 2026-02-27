package io.github.sullis.s3.playground.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.metrics.SdkMetric;

public class MicrometerPublisher
    implements MetricPublisher {
  private static final Logger logger = LoggerFactory.getLogger(MicrometerPublisher.class);

  // visible for testing
  public final MeterRegistry registry;

  public MicrometerPublisher(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void publish(MetricCollection metricCollection) {
    // TODO : WIP
    String name = metricCollection.name();
    logger.info("publish called. name={}", name);
    metricCollection.stream().map(metricRecord -> {
      logger.info("metricRecord: {}", metricRecord);
      SdkMetric sdkMetric = metricRecord.metric();
      logger.info("name={} sdkMetric={} level={}", name, sdkMetric, sdkMetric.level());
      return metricRecord;
    }).toList();
  }

  @Override
  public void close() {
    registry.close();
  }
}
