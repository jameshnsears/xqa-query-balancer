package xqa.resources.messagebroker;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryBalancerEvent {
    private String serviceId = "";
    private long creationTime;
    private String correlationId = "";
    private String digest = "";
    private State state;

    public QueryBalancerEvent(final String serviceId, final String correlationId, final String digest,
            final QueryBalancerEvent.State state) {
        this.serviceId = serviceId;
        this.creationTime = new Date().getTime();
        this.correlationId = correlationId;
        this.digest = digest;
        this.state = state;
    }

    @JsonProperty
    public String getServiceId() {
        return serviceId;
    }

    @JsonProperty
    public long getCreationTime() {
        return creationTime;
    }

    @JsonProperty
    public String getCorrelationId() {
        return correlationId;
    }

    @JsonProperty
    public String getDigest() {
        return digest;
    }

    @JsonProperty
    public QueryBalancerEvent.State getState() {
        return state;
    }

    public enum State {
        @JsonProperty("START")
        START,
        @JsonProperty("END")
        END
    }
}
