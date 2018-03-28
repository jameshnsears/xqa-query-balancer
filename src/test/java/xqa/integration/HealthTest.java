package xqa.integration;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import xqa.XqaDbRestApplication;
import xqa.XqaDbRestConfiguration;

import static org.assertj.core.api.Assertions.fail;

public class HealthTest {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> application = new DropwizardAppRule<>(
            XqaDbRestApplication.class, ResourceHelpers.resourceFilePath("xqa-db-rest.yml"));

    @Test
    public void health() {
        fail("todo - use http://square.github.io/okhttp/ ?");
    }
}
