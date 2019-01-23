package xqa.resources;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import xqa.api.search.SearchDigestReponse;
import xqa.api.search.SearchFilenameResponse;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;
import xqa.api.search.SearchServiceReponse;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResource.class);
    private final Jdbi jdbi;
    private static final String SQLSELECT = "select to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
            + "info->>'source' as filename, " + "info->>'digest' as digest, " + "info->>'serviceId' as service ";
    private static final String SQLFROM = "from events ";
    private static final String SQLORDERBY = "order by creationTime asc;";

    public SearchResource(final Jdbi jdbi) {
        synchronized (this) {
            this.jdbi = jdbi;
        }
    }

    @GET
    @Timed
    public SearchResponse search() {
        final String sql = SQLSELECT + SQLFROM + SQLORDERBY;

        final List<SearchResult> searchResults = getSearchResults(sql);

        final SearchResponse searchResponse = new SearchResponse();
        for (final SearchResult searchResult : searchResults) {
            searchResponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchResponse;
    }

    private synchronized List<SearchResult> getSearchResults(final String sql) {
        final List<SearchResult> searchResults = jdbi.withHandle(handle -> {
            LOGGER.info(sql);
            List<SearchResult> results = new ArrayList<>();
            try {
                results = handle.createQuery(sql).map((rs, ctx) -> new SearchResult(rs.getString("creationTime"),
                        rs.getString("filename"), rs.getString("digest"), rs.getString("service"))).list();
                LOGGER.info(String.format("results.size=%s", results.size()));
            } catch (UnableToExecuteStatementException exception) {
                LOGGER.error(exception.getMessage());
            }
            return results;
        });

        if (searchResults.isEmpty()) {
            throw new WebApplicationException("No results available", Response.Status.NO_CONTENT); // 204
        }

        return searchResults;
    }

    @GET
    @Timed
    @Path("/filename/{filename : .+}")
    public SearchFilenameResponse filename(final @PathParam("filename") String filename) {
        final String sql = SQLSELECT + SQLFROM + "where info->>'source' like '%" + filename
                + "%' and info->>'serviceId' like 'ingest/%' and info->>'state' = 'START' " + SQLORDERBY;

        final List<SearchResult> searchResults = getSearchResults(sql);

        final SearchFilenameResponse searchFilenameResponse = new SearchFilenameResponse();
        for (final SearchResult searchResult : searchResults) {
            searchFilenameResponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchFilenameResponse;
    }

    @GET
    @Timed
    @Path("/digest/{digest : .+}")
    public SearchDigestReponse digest(final @PathParam("digest") String digest) {
        final String sql = SQLSELECT + SQLFROM + "where info->>'digest' like '%" + digest
                + "%' and info->>'state' = 'START' " + SQLORDERBY;

        final List<SearchResult> searchResults = getSearchResults(sql);

        final SearchDigestReponse searchDigestReponse = new SearchDigestReponse();
        for (final SearchResult searchResult : searchResults) {
            searchDigestReponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchDigestReponse;
    }

    @GET
    @Timed
    @Path("/service/{serviceId : .+}")
    public SearchServiceReponse service(final @PathParam("serviceId") String serviceId) {
        final String sql = SQLSELECT + SQLFROM + "where info->>'serviceId' like '%" + serviceId
                + "%' and info->>'state' = 'START' " + SQLORDERBY;

        final List<SearchResult> searchResults = getSearchResults(sql);

        final SearchServiceReponse searchServiceReponse = new SearchServiceReponse();
        for (final SearchResult searchResult : searchResults) {
            searchServiceReponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchServiceReponse;
    }
}
