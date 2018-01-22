package xqa.api.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class SearchResult {
    private String time;
    private String correlationId;
    private String subject;
    private String service;
    private String digest;

    public SearchResult() {
    }

    public SearchResult(String time, String correlationId, String subject, String service, String digest) {
        this.time = time;
        this.correlationId = correlationId;
        this.subject = subject;
        this.service = service;
        this.digest = digest;
    }

    @JsonProperty
    public String getTime() {
        return time;
    }

    @JsonProperty
    public String getCorrelationId() {
        return correlationId;
    }

    @JsonProperty
    public String getSubject() {
        return subject;
    }

    @JsonProperty
    public String getService() {
        return service;
    }

    @JsonProperty
    public String getdigest() {
        return digest;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("time", time).add("correlationId", correlationId)
                .add("subject", subject).add("service", service).add("digest", digest).toString();
    }
}
