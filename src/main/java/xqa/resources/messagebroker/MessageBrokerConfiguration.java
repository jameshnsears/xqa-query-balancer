package xqa.resources.messagebroker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class MessageBrokerConfiguration {
    @NotEmpty
    private String host;

    @NotEmpty
    private int port;

    @NotEmpty
    private String userName;

    @NotEmpty
    private String password;

    @NotEmpty
    private int retryAttempts;

    @NotEmpty
    private String xqueryDestination;

    @NotEmpty
    private String sizeDestination;

    @NotEmpty
    private String auditDestination;

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

    @JsonProperty
    public String getAuditDestination() {
        return auditDestination;
    }

    @JsonProperty
    public void setAuditDestination(String auditDestination) {
        this.auditDestination = auditDestination;
    }

    @JsonProperty
    public String getUserName() {
        return userName;
    }

    @JsonProperty
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public int getRetryAttempts() {
        return retryAttempts;
    }

    @JsonProperty
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getSizeDestination() {
        return sizeDestination;
    }

    @JsonProperty
    public void setSizeDestination(String sizeDestination) {
        this.sizeDestination = sizeDestination;
    }
}
