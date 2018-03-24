package xqa.resources;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);
    private Jdbi jdbi;

    public SearchResource(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @GET
    @Timed
    @Path("/{searchString}")
    public SearchResponse subject(@PathParam("searchString") Optional<String> searchString) {
        logger.debug("searchString={}", searchString.orElse("*"));

        if (searchString.toString().equals("Optional[x]"))
            throw new WebApplicationException(String.format("x"), Response.Status.BAD_REQUEST);

        SearchResponse searchResponse = new SearchResponse();

        searchResponse.getSearchResponse().add(new SearchResult("2018-03-16 17:52:23.259682", "ingest/02bd02c2", "DBER-1923-0416.xml", "aa84010b"));

        return searchResponse;
    }
}
