package xqa.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import javax.ws.rs.BadRequestException;

import org.junit.ClassRule;
import org.junit.Test;

import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaDbRestConfiguration;
import xqa.api.search.SearchResponse;

public class SearchTest {
    private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);

    @ClassRule
    public static final DropwizardAppRule<XqaDbRestConfiguration> RULE = TestSuite.RULE;

    @Test
    public void search() {
        final SearchResponse searchResponse = RULE.client()
                .target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/123").request()
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
        assertThat(searchResponse.getSearchResponse().get(0).toString())
                .isEqualTo("SearchResult{creationTime=2018-03-16 17:52:23.259682, service=ingest/02bd02c2, subject=DBER-1923-0416.xml, digest=aa84010b}");
    }

    @Test
    public void searchFailure() {
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() -> {
            RULE.client().target("http://127.0.0.1:" + RULE.getLocalPort() + "/search/x").request()
                    .get(SearchResponse.class);
        }).withMessage("HTTP 400 Bad Request");
    }
}
