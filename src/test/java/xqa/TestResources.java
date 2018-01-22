package xqa;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectReader;

import io.dropwizard.testing.junit.ResourceTestRule;
import xqa.api.search.SearchResponse;
import xqa.resources.SearchResource;

public class TestResources {
    @Rule
    public final ResourceTestRule resources = ResourceTestRule.builder().addResource(new SearchResource()).build();

    @Test
    public void search() throws IOException {
        String resp = resources.client().target("/search/correlationId/2").request().get(String.class);

        ObjectReader reader = resources.getObjectMapper().readerFor(SearchResponse.class);

        SearchResponse actual = reader.readValue(resp);

        assertThat(actual.getSearchResponse()).size().isEqualTo(1);

        assertThat(actual.getSearchResponse().get(0).toString()).isEqualTo(
                "SearchResult{time=time_1, correlationId=2, subject=subject_3, serviceId=service_4, digest=sha256_5}");
    }
}
