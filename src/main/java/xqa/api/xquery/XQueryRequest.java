package xqa.api.xquery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class XQueryRequest {
    private String xqueryRequest = "";

    public XQueryRequest() {
        // Jackson deserialization
    }

    public XQueryRequest(String xqueryRequest) {
        this.xqueryRequest = xqueryRequest;
    }

    @JsonProperty
    public String getXqueryRequest() {
        return xqueryRequest;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("xqueryRequest", xqueryRequest).toString();
    }
}
