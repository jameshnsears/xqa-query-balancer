package xqa.resources.messagebroker;

import java.util.Date;

class QueryBalancerEvent {
    private final String serviceId;
    private final long creationTime;
    private final String correlationId;
    private final String poolSize;
    private final String digest;
    private final String state;

    public QueryBalancerEvent(final String serviceId,
                              final String correlationId,
                              final String poolSize,
                              final String digest,
                              final String state) {
        this.serviceId = serviceId;
        this.creationTime = new Date().getTime();
        this.correlationId = correlationId;
        this.poolSize = poolSize;
        this.digest = digest;
        this.state = state;
    }
}
