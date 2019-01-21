package xqa.unit;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import xqa.resources.SearchResource;

import java.util.Optional;

public class SearchTest {
    @Test
    public void searchFilename() {
        SearchResource searchResource = new SearchResource(Mockito.mock(Jdbi.class));

        searchResource.filename(Optional.of("/xml/DAQU-1931-0321.xml"));

        /*
                Client client = JerseyClientBuilder.createClient();

        final SearchResponse searchResponse = client
                .target("http://127.0.0.1:" + application.getLocalPort() + "/search/d6f04c9881").request()
                .get(SearchResponse.class);

        final String expected = objectMapper.writeValueAsString(
                objectMapper.readValue(fixture("response/searchFilename.json"), SearchResult[].class));

        assertThat(objectMapper.writeValueAsString(searchResponse.getSearchResponse()))
                .isEqualTo(expected);
         */
    }

    @Test
    public void searchDigest() {

    }

    @Test
    public void searchService() {

    }
}
