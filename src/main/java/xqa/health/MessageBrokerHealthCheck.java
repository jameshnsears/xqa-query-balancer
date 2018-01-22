package xqa.health;

import com.codahale.metrics.health.HealthCheck;

public class MessageBrokerHealthCheck extends HealthCheck {

    public MessageBrokerHealthCheck() {
    }

    @Override
    protected Result check() {
        return Result.healthy();
    }
}
