package xqa.integration;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import xqa.XqaDbRestApplication;
import xqa.XqaDbRestConfiguration;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SetupDatabase.class, SearchTest.class, StatusTest.class, XQueryTest.class })
public class TestSuite {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = new DropwizardAppRule<>(
            XqaDbRestApplication.class, ResourceHelpers.resourceFilePath("xqa-db-rest.yml"));
}
