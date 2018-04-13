package xqa.integration;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientProperties;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

public class XQueryTest {
  @ClassRule
  public static final DropwizardAppRule<XqaQueryBalancerConfiguration> application = new DropwizardAppRule<>(
      XqaQueryBalancerApplication.class,
      ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));
  private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);
  private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

  @Test
  public void xquery() throws IOException {
    final XQueryResponse xqueryResponse = application.client()
        .property(ClientProperties.READ_TIMEOUT, 6000)
        .target("http://127.0.0.1:" + application.getLocalPort() + "/xquery")
        .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
        .readEntity(XQueryResponse.class);

    final String expected = objectMapper.writeValueAsString(
        objectMapper.readValue(fixture("fixtures/xquery.json"), XQueryResponse.class));

    assertThat(objectMapper.writeValueAsString(xqueryResponse)).isEqualTo(expected);
  }
}
