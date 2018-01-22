package xqa.api.status;

import java.util.List;

import com.google.common.collect.Lists;

public class StatusResponse {
    private List<StatusResult> statusResult = Lists.newArrayList();

    public StatusResponse() {
    }

    public StatusResponse(List<StatusResult> statusResult) {
        this.statusResult = statusResult;
    }

    public List<StatusResult> getSearchResponse() {
        return this.statusResult;
    }

    public void setSearchResponse(List<StatusResult> statusResult) {
        this.statusResult = statusResult;
    }
}
