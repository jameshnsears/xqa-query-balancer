package xqa.resources.messagebroker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class MessageBrokerConfiguration {
    @NotEmpty
    private String host;

    @NotEmpty
    private String xqueryDestination;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public String getXqueryDestination() {
        return xqueryDestination;
    }

    @JsonProperty
    public void setXqueryDestination(String xqueryDestination) {
        this.xqueryDestination = xqueryDestination;
    }
}
