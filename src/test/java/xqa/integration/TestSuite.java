package xqa.integration;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaRestApplication;
import xqa.XqaRestConfiguration;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SearchTest.class, StatusTest.class, XQueryTest.class })
public class TestSuite {
    @ClassRule
    public static final DropwizardAppRule<XqaRestConfiguration> RULE = new DropwizardAppRule<>(
            XqaRestApplication.class, ResourceHelpers.resourceFilePath("xqa-db-rest.yml"));
}
