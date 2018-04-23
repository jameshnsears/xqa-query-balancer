package xqa.integration.fixtures;

public interface Fixture {
    String getResource();
    void setupStorage() throws Exception;
}
