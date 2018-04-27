package xqa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.integration.fixtures.ShardFixture;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

public class XQueryTest extends ShardFixture {
    @ClassRule
    public static final DropwizardAppRule<XqaQueryBalancerConfiguration> application = new DropwizardAppRule<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));
    private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void xquery() throws Exception {
        setupStorage(application.getConfiguration());

        final XQueryResponse xqueryResponse = application.client()
                .property(ClientProperties.READ_TIMEOUT, 10000)
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        // https://regex101.com/
        String regExMatcher = "\\\"<xqueryResponse>\\\\n<shard id='[a-zA-Z0-9]{8}'>\\\\n[0-9]{1,2}\\\\n<\\/shard>\\\\n<shard id='[a-zA-Z0-9]{8}'>\\\\n[0-9]{1,2}\\\\n<\\/shard>\\\\n<\\/xqueryResponse>\\\"";

        System.out.println(objectMapper.writeValueAsString(xqueryResponse.getXqueryResponse()));
        assertThat(objectMapper.writeValueAsString(xqueryResponse.getXqueryResponse())).matches(regExMatcher);
    }
}
