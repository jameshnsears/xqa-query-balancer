package xqa.api.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResult {
    private String creationTime;
    private String serviceId;
    private String subject;
    private String digest;

    public SearchResult() {
    }

    public SearchResult(String creationTime, String serviceId, String subject, String digest) {
        this.creationTime = creationTime;
        this.serviceId = serviceId;
        this.subject = subject;
        this.digest = digest;
    }

    @JsonProperty
    public String getCreationTime() {
        return creationTime;
    }

    @JsonProperty
    public String getSubject() {
        return subject;
    }

    @JsonProperty
    public String getServiceId() {
        return serviceId;
    }

    @JsonProperty
    public String getdigest() {
        return digest;
    }
}
