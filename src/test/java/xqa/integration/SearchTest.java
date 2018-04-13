package xqa.integration;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

import javax.ws.rs.BadRequestException;

import org.junit.ClassRule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;

public class SearchTest extends DatabaseFixture {
  @ClassRule
  public static final DropwizardAppRule<XqaQueryBalancerConfiguration> application = new DropwizardAppRule<>(
      XqaQueryBalancerApplication.class,
      ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));
  private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

  @Test
  public void search() throws Exception {
    setupDatabase();

    final SearchResponse searchResponse = application.client()
        .target("http://127.0.0.1:" + application.getLocalPort() + "/search/d6f04c9881").request()
        .get(SearchResponse.class);

    final String expected = objectMapper.writeValueAsString(
        objectMapper.readValue(fixture("fixtures/search.json"), SearchResult[].class));

    assertThat(objectMapper.writeValueAsString(searchResponse.getSearchResponse()))
        .isEqualTo(expected);
  }

  @Test
  public void searchFailure() {
    assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> application.client()
        .target("http://127.0.0.1:" + application.getLocalPort() + "/search/0123456789").request()
        .get(SearchResponse.class)).withMessage("HTTP 400 Bad Request");
  }

  @Test
  public void searchWithSlash() throws Exception {
    setupDatabase();

    final SearchResponse searchResponse = application.client()
        .target("http://127.0.0.1:" + application.getLocalPort() + "/search/shard/e540188c")
        .request().get(SearchResponse.class);

    assertEquals(40, searchResponse.getSearchResponse().size());
  }
}
