package xqa.resources;

import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);

    private SearchResponse search(final Optional<String> correlationId, final Optional<String> subject,
            final Optional<String> service, final Optional<String> digest) {

        logger.debug("correlationId={}", correlationId.orElse("N/A"));
        logger.debug("service={}", service.orElse("N/A"));
        logger.debug("subject={}", subject.orElse("N/A"));
        logger.debug("digest={}", digest.orElse("N/A"));

        if (correlationId.toString().equals("Optional[2]"))
            throw new WebApplicationException(String.format("correlationId"), Response.Status.BAD_REQUEST);

        SearchResponse searchResponse = new SearchResponse();
        SearchResult searchResult = new SearchResult("time", correlationId.orElse("N/A"), subject.orElse("N/A"),
                service.orElse("N/A"), digest.orElse("N/A"));
        searchResponse.getSearchResponse().add(searchResult);

        return searchResponse;
    }

    @GET
    @Timed
    @Path("/correlationId/{correlationId}")
    public SearchResponse correlationId(@PathParam("correlationId") Optional<String> correlationId) {
        return search(correlationId, Optional.empty(), Optional.empty(), Optional.empty());
    }

    @GET
    @Timed
    @Path("/digest/{digest}")
    public SearchResponse digest(@PathParam("digest") Optional<String> digest) {
        return search(Optional.empty(), Optional.empty(), Optional.empty(), digest);
    }

    @GET
    @Timed
    @Path("/service/{service}")
    public SearchResponse service(@PathParam("service") Optional<String> service) {
        return search(Optional.empty(), Optional.empty(), service, Optional.empty());
    }

    @GET
    @Timed
    @Path("/subject/{subject}")
    public SearchResponse subject(@PathParam("subject") Optional<String> subject) {
        return search(Optional.empty(), subject, Optional.empty(), Optional.empty());
    }
}
