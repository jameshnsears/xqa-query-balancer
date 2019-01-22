package xqa.health;

import com.codahale.metrics.health.HealthCheck;

public class QueryBalancerHealthCheck extends HealthCheck {
    @Override
    protected Result check() {
        return Result.healthy();
    }
}
