package xqa;

import com.fasterxml.jackson.databind.ObjectReader;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;
import xqa.api.search.SearchResponse;
import xqa.resources.SearchResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TestResources {
    @Test
    public void search() throws IOException {
        final Jdbi jdbi = mock(Jdbi.class);
        // http://jdbi.org/

        // or
        // Jdbi jdbi = Jdbi.create("jdbc:h2:mem:test"); // (H2 in-memory database)
        /*

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>1.4.197</version>
            <scope>test</scope>
        </dependency>
         */

        final ResourceTestRule resources = ResourceTestRule.builder().addResource(new SearchResource(jdbi)).build();

        String resp = resources.client().target("/search/2").request().get(String.class);

        ObjectReader reader = resources.getObjectMapper().readerFor(SearchResponse.class);

        SearchResponse actual = reader.readValue(resp);

        assertThat(actual.getSearchResponse()).size().isEqualTo(1);

        assertThat(actual.getSearchResponse().get(0).toString()).isEqualTo(
                "SearchResult{time=time_1, correlationId=2, subject=subject_3, serviceId=service_4, digest=sha256_5}");
    }
}
