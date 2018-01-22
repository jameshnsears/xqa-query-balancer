package xqa.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaRestConfiguration;
import xqa.api.status.StatusResponse;

public class StatusTest {
    @ClassRule
    public static final DropwizardAppRule<XqaRestConfiguration> RULE = TestSuite.RULE;

    @Test
    public void status() {
        final StatusResponse statusResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/status").request().get(StatusResponse.class);

        assertThat(statusResponse.getSearchResponse().get(0).toString()).isEqualTo(
                "StatusResult{treeNode=true, service=xqa-ingest-balancer-uuid-1, items=40, lastItemTimestamp=2018010914020, pingable=true}");
    }
}
