package xqa.api.xquery;

import com.fasterxml.jackson.annotation.JsonProperty;

public class XQueryResponse {
    private String xqueryResponse = "";

    public XQueryResponse() {
        // Jackson deserialization
    }

    public XQueryResponse(final String xqueryResponse) {
        this.xqueryResponse = xqueryResponse;
    }

    @JsonProperty
    public String getXqueryResponse() {
        return xqueryResponse;
    }
}
