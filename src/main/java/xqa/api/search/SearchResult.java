package xqa.api.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

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
        if (subject == null)
            return "";
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("creationTime", creationTime).add("serviceId", serviceId).add("subject", subject).add("digest", digest).toString();
    }
}
