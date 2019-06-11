package com.mapbox.android.core.metrics;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCompositeMetricsTest {
  private TestCompositeMetrics metrics;

  @Before
  public void setUp() {
    metrics = new TestCompositeMetrics(TimeUnit.HOURS.toMillis(1));
  }

  @Test
  public void addLongSpanMetric() {
    metrics.add("test", 100L);
    metrics.add("test", 10L);
    Metrics firstMetric = metrics.getMetrics("test");
    Metrics secondMetric = metrics.getMetrics("test");
    assertThat(firstMetric.getValue()).isEqualTo(110L);
    assertThat(secondMetric).isNull();
  }

  @Test
  public void addMultipleMetrics() {
    metrics.add("test", 100L);
    metrics.add("foo", 10L);
    Metrics test = metrics.getMetrics("test");
    Metrics foo = metrics.getMetrics("foo");
    assertThat(test.getValue()).isEqualTo(100L);
    assertThat(foo.getValue()).isEqualTo(10L);
  }

  @Test
  public void addRemoveMetric() {
    metrics.add("test", 100L);
    metrics.getMetrics("test");
    metrics.add("test", 10L);
    assertThat(metrics.getMetrics("test").getValue()).isEqualTo(10L);
  }

  @Test
  public void getEmptyMetric() {
    assertThat(metrics.getMetrics("test")).isNull();
  }

  @Test
  public void getMetricsEmptyString() {
    assertThat(metrics.getMetrics("")).isNull();
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