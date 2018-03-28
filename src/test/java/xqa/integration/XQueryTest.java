package xqa.integration;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import xqa.XqaDbRestConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class XQueryTest {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = TestSuite.configuration;

    @Test
    public void xquery() {
        final XQueryResponse xqueryResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/xquery").request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(new XQueryRequest("count(/)"))).readEntity(XQueryResponse.class);

        /* POST
{
    "xqueryResponse": "<some xquery response/>"
}
         */

        assertThat(xqueryResponse.xqueryRespone).isEqualTo("<a>b</a>");
    }
}
