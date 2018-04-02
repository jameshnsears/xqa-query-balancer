package xqa.resources.messagebroker;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class QueryBalancerEvent {
    private String serviceId = "";
    private long creationTime = 0;
    private String correlationId = "";
    private String digest = "";
    private String state = "";

    public QueryBalancerEvent() { }
        // Jackson deserialization

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
    public String getState() {
        return state;
    }
}
