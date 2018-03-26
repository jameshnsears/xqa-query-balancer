package xqa.core;

public class SearchPOJO {
    public String creationTime;
    public String serviceId;
    public String source;
    public String digest;

    public SearchPOJO(String creationTime, String serviceId, String source, String digest) {
        this.creationTime = creationTime;
        this.serviceId = serviceId;
        this.source = source;
        this.digest = digest;
    }
}
