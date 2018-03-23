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

        /*
[
    {
        "data": {
            "service": "ingest/02bd02c2",
            "items": "40",
            "lastItemItemstamp": "2018-03-16 17:52:23.247860",
            "pingable": ""
        },
        "children": [
            {
                "data": {
                    "service": "ingestbalancer/6336fca0",
                    "items": "40",
                    "lastItemItemstamp": "2018-03-16 17:57:19.023025",
                    "pingable": "Y"
                }
            },
            {
                "data": {
                    "service": "shard/ee251962",
                    "items": "40",
                    "lastItemItemstamp": "2018-03-16 17:57:19.028748",
                    "pingable": "Y"
                }
            }
        ]
    }
]

         */
        assertThat(statusResponse.getSearchResponse().get(0).toString()).isEqualTo(
                "StatusResult{treeNode=true, service=xqa-ingest-balancer-uuid-1, items=40, lastItemTimestamp=2018010914020, pingable=true}");
    }
}
