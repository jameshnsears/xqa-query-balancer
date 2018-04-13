package xqa.health;

import com.codahale.metrics.health.HealthCheck;

public class QueryBalancerHealthCheck extends HealthCheck {
  public QueryBalancerHealthCheck() {
  }

  @Override
  protected Result check() {
    return Result.healthy();
  }
}
