package xqa.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.ws.rs.BadRequestException;

import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.DropwizardAppRule;
import xqa.XqaRestConfiguration;
import xqa.api.search.SearchResponse;

public class SearchTest {
    @ClassRule
    public static final DropwizardAppRule<XqaRestConfiguration> RULE = TestSuite.RULE;

    @Test
    public void correlationId() {
        final SearchResponse correlationIdSearchResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/correlationId/1").request()
                .get(SearchResponse.class);

        assertThat(correlationIdSearchResponse.getSearchResponse().get(0).toString())
                .isEqualTo("SearchResult{time=time, correlationId=1, subject=N/A, service=N/A, digest=N/A}");
    }

    @Test
    public void correlationIdFailure() {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            RULE.client().target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/correlationId/2").request()
                    .get(SearchResponse.class);
        }).withMessage("HTTP 400 Bad Request");
    }

    @Test
    public void digest() {
        final SearchResponse digestResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/digest/1").request()
                .get(SearchResponse.class);

        assertThat(digestResponse.getSearchResponse().get(0).toString())
                .isEqualTo("SearchResult{time=time, correlationId=N/A, subject=N/A, service=N/A, digest=1}");
    }

    @Test
    public void service() {
        final SearchResponse serviceResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/service/1").request()
                .get(SearchResponse.class);

        assertThat(serviceResponse.getSearchResponse().get(0).toString())
                .isEqualTo("SearchResult{time=time, correlationId=N/A, subject=N/A, service=1, digest=N/A}");

    }

    @Test
    public void subject() {
        final SearchResponse subjectResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/subject/1").request()
                .get(SearchResponse.class);

        assertThat(subjectResponse.getSearchResponse().get(0).toString())
                .isEqualTo("SearchResult{time=time, correlationId=N/A, subject=1, service=N/A, digest=N/A}");

    }
}
