package xqa.api.xquery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class XQueryResponse {
    public String xqueryRespone = "";

    public XQueryResponse() {
        // Jackson deserialization
    }

    public XQueryResponse(String xqueryRespone) {
        this.xqueryRespone = xqueryRespone;
    }

    @JsonProperty
    public String getXqueryRespone() {
        return xqueryRespone;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("xqueryRespone", xqueryRespone).toString();
    }
}
