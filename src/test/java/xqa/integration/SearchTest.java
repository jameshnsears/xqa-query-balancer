package xqa.integration;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.sql.SQLException;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.docker.DockerClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.jackson.Jackson;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;
import xqa.integration.fixtures.DatabaseFixture;

public class SearchTest extends DatabaseFixture {
    private static final ObjectMapper OBJECTMAPPER = Jackson.newObjectMapper();
    private final Client client = ClientBuilder.newClient();

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient = new DockerClient();

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);
        APPLICATION.before();
    }

    @AfterAll
    public static void stopcontainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient.rmContainers(configurationAccessor);
        APPLICATION.after();
    }

    @Test
    public void search() throws SQLException, ClassNotFoundException, IOException {
        storageEmpty();
        storagePopulate();

        final String searchUrl = "http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search";

        final SearchResponse searchWithoutTrailingSlash = client
                .target(searchUrl)
                .request()
                .get(SearchResponse.class);

        Assertions.assertEquals(240, searchWithoutTrailingSlash.getSearchResponse().size());

        final SearchResponse searchWithTrailingSlash = client
                .target(searchUrl)
                .request()
                .get(SearchResponse.class);

        Assertions.assertEquals(OBJECTMAPPER.writeValueAsString(searchWithoutTrailingSlash.getSearchResponse()),
                OBJECTMAPPER.writeValueAsString(searchWithTrailingSlash.getSearchResponse()));
    }

    @Test
    public void searchFailure() throws SQLException, ClassNotFoundException {
        storageEmpty();

        Assertions.assertEquals(204, client.target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search")
                .request().get().getStatus());
    }

    @Test
    public void searchFilename() throws SQLException, ClassNotFoundException, IOException {
        storageEmpty();
        storagePopulate();

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> client.target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search/filename/")
                        .request().get(SearchResponse.class))
                .withMessage("HTTP 404 Not Found");

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search/filename//xml/DAQU-1931-0321.xml")
                .request().get(SearchResponse.class);

        Assertions.assertEquals(1, searchResponse.getSearchResponse().size());

        final String expected = OBJECTMAPPER.writeValueAsString(
                OBJECTMAPPER.readValue(fixture("response/searchFilename.json"), SearchResult[].class));

        assertThat(OBJECTMAPPER.writeValueAsString(searchResponse.getSearchResponse())).isEqualTo(expected);

        final WebTarget target = client.target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search/filename/blah");
        final Response response = target.request().get();

        Assertions.assertEquals(204, response.getStatus());
    }

    @Test
    public void searchDigest() throws SQLException, ClassNotFoundException, IOException {
        storageEmpty();
        storagePopulate();

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search/digest/d6f04c988162284ff57c06e69")
                .request().get(SearchResponse.class);

        Assertions.assertEquals(3, searchResponse.getSearchResponse().size());

        final String expected = OBJECTMAPPER.writeValueAsString(
                OBJECTMAPPER.readValue(fixture("response/searchDigest.json"), SearchResult[].class));

        assertThat(OBJECTMAPPER.writeValueAsString(searchResponse.getSearchResponse())).isEqualTo(expected);
    }

    @Test
    public void searchService() throws SQLException, ClassNotFoundException, IOException {
        storageEmpty();
        storagePopulate();

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + APPLICATION.getLocalPort() + "/search/service/ingest/d6d26946").request()
                .get(SearchResponse.class);

        Assertions.assertNotNull(searchResponse);
        Assertions.assertEquals(40, searchResponse.getSearchResponse().size());

        final String expected = OBJECTMAPPER.writeValueAsString(
                OBJECTMAPPER.readValue(fixture("response/searchService.json"), SearchResult[].class));

        assertThat(OBJECTMAPPER.writeValueAsString(searchResponse.getSearchResponse())).isEqualTo(expected);
    }
}
