package xqa.resources.messagebroker;

import java.util.Date;

public class QueryBalancerEvent {
    private final String serviceId;
    private final long creationTime;
    private final String correlationId;
    private final String digest;
    private final String state;

    public QueryBalancerEvent(final String serviceId,
                              final String correlationId,
                              final String digest,
                              final String state) {
        this.serviceId = serviceId;
        this.creationTime = new Date().getTime();
        this.correlationId = correlationId;
        this.digest = digest;
        this.state = state;
    }
}
