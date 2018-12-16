package xqa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;
import xqa.integration.fixtures.DatabaseFixture;

import javax.ws.rs.BadRequestException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SearchTest extends DatabaseFixture {
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void search() throws Exception {
        setupStorage();

        final SearchResponse searchResponse = application.client()
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/d6f04c9881").request()
                .get(SearchResponse.class);

        final String expected = objectMapper.writeValueAsString(
                objectMapper.readValue(fixture("response/search.json"), SearchResult[].class));

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
        setupStorage();

        final SearchResponse searchResponse = application.client()
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/shard/e540188c")
                .request().get(SearchResponse.class);

        Assertions.assertEquals(40, searchResponse.getSearchResponse().size());
    }
}
