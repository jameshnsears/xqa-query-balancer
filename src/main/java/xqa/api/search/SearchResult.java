package xqa.api.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class SearchResult {
    private String creationTime;
    private String service;
    private String subject;
    private String digest;

    public SearchResult() {
    }

    public SearchResult(String creationTime, String service, String subject, String digest) {
        this.creationTime = creationTime;
        this.service = service;
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
    public String getService() {
        return service;
    }

    @JsonProperty
    public String getdigest() {
        return digest;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("creationTime", creationTime).add("service", service).add("subject", subject).add("digest", digest).toString();
    }
}
