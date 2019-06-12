package com.mapbox.android.core.metrics;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class AbstractCompositeMetricsInstrumentedTest {
  @Test
  public void addShortSpanMetric() throws InterruptedException {
    TestCompositeMetrics metrics = new TestCompositeMetrics(TimeUnit.SECONDS.toMillis(1));
    metrics.add("test", 100L);
    Thread.sleep(1000L);
    metrics.add("test", 10L);
    Metrics firstMetric = metrics.getMetrics("test");
    Metrics secondMetric = metrics.getMetrics("test");
    assertEquals(100L, firstMetric.getValue());
    assertEquals(10L, secondMetric.getValue());
  }

  private static final class TestCompositeMetrics extends AbstractCompositeMetrics {
    TestCompositeMetrics(long maxLength) {
      super(maxLength);
    }

    @Override
    protected Metrics nextMetrics(long start, long end) {
      return new MetricsImpl(start, end);
    }
  }
}