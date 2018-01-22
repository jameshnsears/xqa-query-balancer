package xqa.api.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class StatusResult {
    private Boolean treeNode;
    private String service;
    private long items;
    private String lastItemTimestamp;
    private Boolean pingable;

    public StatusResult() {
    }

    public StatusResult(Boolean treeNode, String service, int items, String lastItemTimestamp, Boolean pingable) {
        this.treeNode = treeNode;
        this.service = service;
        this.items = items;
        this.lastItemTimestamp = lastItemTimestamp;
        this.pingable = pingable;
    }

    @JsonProperty
    public Boolean getTreeNode() {
        return treeNode;
    }

    @JsonProperty
    public String getService() {
        return service;
    }

    @JsonProperty
    public long getItems() {
        return items;
    }

    @JsonProperty
    public String getLastItemTimestamp() {
        return lastItemTimestamp;
    }

    @JsonProperty
    public Boolean getPingable() {
        return pingable;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("treeNode", treeNode).add("service", service).add("items", items)
                .add("lastItemTimestamp", lastItemTimestamp).add("pingable", pingable).toString();
    }
}
