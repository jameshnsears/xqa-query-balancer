package xqa.integration.fixtures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShardFixture implements Fixture {
    private static final Logger logger = LoggerFactory.getLogger(ShardFixture.class);

    public String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("populate-shard")
                .getPath();
    }

    public void setupStorage() throws Exception {
        // truncate
        // populate
    }
}
