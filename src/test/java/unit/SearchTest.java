package unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.docker.DockerClient;
import io.dropwizard.jackson.Jackson;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.*;
import unit.fixtures.DatabaseFixture;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.client.Client;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class SearchTest extends DatabaseFixture {
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) {
        dockerClient = new DockerClient();

        try {
            dockerClient.pull(configurationAccessor.images());
            dockerClient.startContainers(configurationAccessor);
            application.before();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void stopcontainers(final ConfigurationAccessor configurationAccessor) {
        try {
            dockerClient.rmContainers(configurationAccessor);

            application.after();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() throws Exception {
        setupStorage();

        Client client = JerseyClientBuilder.createClient();

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/d6f04c9881").request()
                .get(SearchResponse.class);

        final String expected = objectMapper.writeValueAsString(
                objectMapper.readValue(fixture("response/search.json"), SearchResult[].class));

        assertThat(objectMapper.writeValueAsString(searchResponse.getSearchResponse()))
                .isEqualTo(expected);
    }

    @Test
    public void searchFailure() {
        Client client = JerseyClientBuilder.createClient();

        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> client
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/0123456789").request()
                .get(SearchResponse.class)).withMessage("HTTP 400 Bad Request");
    }

    @Test
    public void searchWithSlash() throws Exception {
        setupStorage();

        Client client = JerseyClientBuilder.createClient();

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/shard/e540188c")
                .request().get(SearchResponse.class);

        Assertions.assertEquals(40, searchResponse.getSearchResponse().size());
    }
}
