package xqa.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaDbRestConfiguration;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;

import javax.ws.rs.BadRequestException;
import java.io.IOException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SearchTest {
    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> application = TestSuite.configuration;
    private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void search() throws IOException {
        final SearchResponse searchResponse = application.client()
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/d6f04c9881").request()
                .get(SearchResponse.class);

        assertThat(searchResponse.getSearchResponse().size() == 6);

        final String expected = objectMapper.writeValueAsString(objectMapper.readValue(fixture("fixtures/search.json"), SearchResult[].class));

        assertThat(objectMapper.writeValueAsString(searchResponse.getSearchResponse())).isEqualTo(expected);
    }

    @Test
    public void searchFailure() {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            application.client().target("http://127.0.0.1:" + application.getLocalPort() + "/search/0123456789").request()
                    .get(SearchResponse.class);
        }).withMessage("HTTP 400 Bad Request");
    }
}
