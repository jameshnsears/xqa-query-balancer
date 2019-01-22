package xqa.resources.messagebroker;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private String ingestDestination;

    @NotEmpty
    private String auditDestination;

    @NotEmpty
    private int shardResponseTimeout;

    @NotEmpty
    private int shardResponseSecondaryTimeout;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(final String host) {
        this.host = host;
    }

    @JsonProperty
    public String getXqueryDestination() {
        return xqueryDestination;
    }

    @JsonProperty
    public void setXqueryDestination(final String xqueryDestination) {
        this.xqueryDestination = xqueryDestination;
    }

    @JsonProperty
    public String getAuditDestination() {
        return auditDestination;
    }

    @JsonProperty
    public void setAuditDestination(final String auditDestination) {
        this.auditDestination = auditDestination;
    }

    @JsonProperty
    public String getUserName() {
        return userName;
    }

    @JsonProperty
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(final String password) {
        this.password = password;
    }

    @JsonProperty
    public int getRetryAttempts() {
        return retryAttempts;
    }

    @JsonProperty
    public void setRetryAttempts(final int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(final int port) {
        this.port = port;
    }

    @JsonProperty
    public String getIngestDestination() {
        return ingestDestination;
    }

    @JsonProperty
    public void setIngestDestination(final String ingestDestination) {
        this.ingestDestination = ingestDestination;
    }

    @JsonProperty
    public int getShardResponseTimeout() {
        return shardResponseTimeout;
    }

    @JsonProperty
    public void setShardResponseTimeout(final int shardResponseTimeout) {
        this.shardResponseTimeout = shardResponseTimeout;
    }

    @JsonProperty
    public int getShardResponseSecondaryTimeout() {
        return shardResponseSecondaryTimeout;
    }

    @JsonProperty
    public void setShardResponseSecondaryTimeout(final int shardResponseSecondaryTimeout) {
        this.shardResponseSecondaryTimeout = shardResponseSecondaryTimeout;
    }
}
