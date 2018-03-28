package xqa.integration;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import xqa.XqaDbRestConfiguration;

import static org.assertj.core.api.Assertions.fail;

public class HealthTest {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = TestSuite.configuration;

    @Test
    public void health() {
        fail("todo - use http://square.github.io/okhttp/ ?");
    }
}
