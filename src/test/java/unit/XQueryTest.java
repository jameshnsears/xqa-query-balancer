package unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.assertj.core.api.Assertions;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unit.fixtures.ShardFixture;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@ExtendWith(ConfigurationParameterResolver.class)
public class XQueryTest extends ShardFixture {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> application = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    private DockerClient dockerClient;

    @BeforeEach
    public void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient = new DockerClient();
        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);

        application.before();
    }

    @AfterEach
    public void stopcontainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient.rmContainers(configurationAccessor);

        application.after();
    }

    @Test
    public void xquery() throws Exception {
        setupStorage(application.getConfiguration());

        Client client = JerseyClientBuilder.createClient();

        final XQueryResponse xqueryResponse = client
                .property(ClientProperties.READ_TIMEOUT, 10000)
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        // https://regex101.com/
        String regExMatcher = "\\\"<xqueryResponse>\\\\n<shard id='[a-zA-Z0-9]{8}'>\\\\n[0-9]{1,2}\\\\n<\\/shard>\\\\n<shard id='[a-zA-Z0-9]{8}'>\\\\n[0-9]{1,2}\\\\n<\\/shard>\\\\n<\\/xqueryResponse>\\\"";

        System.out.println(objectMapper.writeValueAsString(xqueryResponse.getXqueryResponse()));
        Assertions.assertThat(objectMapper.writeValueAsString(xqueryResponse.getXqueryResponse()))
                .matches(regExMatcher);
    }
}
