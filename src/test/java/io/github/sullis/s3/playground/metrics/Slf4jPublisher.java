package io.github.sullis.s3.playground.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.metrics.MetricCollection;
import software.amazon.awssdk.metrics.MetricPublisher;

public class Slf4jPublisher
    implements MetricPublisher {

  private final Logger logger = LoggerFactory.getLogger(Slf4jPublisher.class);

  public Slf4jPublisher() { }

  @Override
  public void publish(MetricCollection metricCollection) {
    logger.info("publish called. metricCollection: {}", metricCollection);
  }

  @Override
  public void close() {
    // no-op
  }
}
