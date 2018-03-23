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
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search?searchValue=123").request()
                .get(SearchResponse.class);

        /*
[
        {
            "creationTime": "2018-03-16 17:52:23.259682",
            "service": "ingest/02bd02c2",
            "subject": "DBER-1923-0416.xml",
            "digest": "aa84010b"
        }
]
         */
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
}
