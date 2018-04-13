package xqa.api.xquery;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class XQueryResponse {
  private String xqueryResponse = "";

  public XQueryResponse() {
    // Jackson deserialization
  }

  public XQueryResponse(String xqueryResponse) {
    this.xqueryResponse = xqueryResponse;
  }

  @JsonProperty
  public String getXqueryResponse() {
    return xqueryResponse;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("xqueryResponse", xqueryResponse).toString();
  }
}
