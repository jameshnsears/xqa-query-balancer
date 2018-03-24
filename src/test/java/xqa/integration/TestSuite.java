package xqa.integration;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaDbRestApplication;
import xqa.XqaDbRestConfiguration;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SearchTest.class, StatusTest.class, XQueryTest.class })
public class TestSuite {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = new DropwizardAppRule<>(
            XqaDbRestApplication.class, ResourceHelpers.resourceFilePath("xqa-db-rest.yml"));
}
