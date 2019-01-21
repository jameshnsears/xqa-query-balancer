package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.search.SearchDigestReponse;
import xqa.api.search.SearchFilenameResponse;
import xqa.api.search.SearchResponse;
import xqa.api.search.SearchResult;
import xqa.api.search.SearchServiceReponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {
    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);
    private final Jdbi jdbi;

    public SearchResource(Jdbi jdbi) {
        synchronized (this) {
            this.jdbi = jdbi;
        }
    }

    @GET
    @Timed
    @Path("/")
    public synchronized SearchResponse search() {
        String sql = "select to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
                + "info->>'source' as filename, "
                + "info->>'digest' as digest, "
                + "info->>'serviceId' as service "
                + "from events "
                + "order by creationTime asc;";

        List<SearchResult> searchResults = getSearchResults(sql);

        SearchResponse searchResponse = new SearchResponse();
        for (SearchResult searchResult : searchResults) {
            searchResponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchResponse;
    }

    private List<SearchResult> getSearchResults(final String sql) {
        List<SearchResult> searchResults = jdbi.withHandle(handle -> {
                logger.info(sql);
                return handle.createQuery(sql).map((rs, ctx) -> new SearchResult(
                        rs.getString("creationTime"),
                        rs.getString("filename"),
                        rs.getString("digest"),
                        rs.getString("service"))).list();
            });

        if (searchResults.isEmpty()) {
            throw new WebApplicationException("No results available", Response.Status.NO_CONTENT);  // 204
        }

        return searchResults;
    }

    @GET
    @Timed
    @Path("/filename{filename : .+}")
    public synchronized SearchFilenameResponse filename(
            @PathParam("filename") Optional<String> filename) {
        if (filename.get().equals("/")) {
            logger.warn("filename missing");
            throw new WebApplicationException("filename missing", Response.Status.BAD_REQUEST);
        }

        logger.debug("filename={}", filename.get());

        String sql ="select to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
                + "info->>'source' as filename, "
                + "info->>'digest' as digest, "
                + "info->>'serviceId' as service "
                + "from events "
                + "where info->>'source' like '%"
                + filename.get() + "%' and info->>'serviceId' like 'ingest/%' and info->>'state' = 'START' "
                + "order by creationTime asc;";

        List<SearchResult> searchResults = getSearchResults(sql);

        SearchFilenameResponse searchFilenameResponse = new SearchFilenameResponse();
        for (SearchResult searchResult : searchResults) {
            searchFilenameResponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchFilenameResponse;
    }

    @GET
    @Timed
    @Path("/digest{digest : .+}")
    public synchronized SearchDigestReponse digest(
            @PathParam("digest") Optional<String> digest) {
        if (digest.get().isEmpty()) {
            logger.warn("digest missing");
            throw new WebApplicationException("digest missing", Response.Status.BAD_REQUEST);
        }

        logger.debug("digest={}", digest.get());

        String sql ="select to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
                + "info->>'source' as filename, "
                + "info->>'digest' as digest, "
                + "info->>'serviceId' as service "
                + "from events "
                + "where info->>'digest' like '%" + digest.get() + "%' and info->>'state' = 'START' "
                + "order by creationTime asc;";

        List<SearchResult> searchResults = getSearchResults(sql);

        SearchDigestReponse searchDigestReponse = new SearchDigestReponse();
        for (SearchResult searchResult : searchResults) {
            searchDigestReponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchDigestReponse;
    }

    @GET
    @Timed
    @Path("/service{serviceId : .+}")
    public synchronized SearchServiceReponse service(
            @PathParam("serviceId") Optional<String> serviceId) {
        if (serviceId.get().isEmpty()) {
            logger.warn("serviceId missing");
            throw new WebApplicationException("serviceId missing", Response.Status.BAD_REQUEST);
        }

        logger.debug("serviceId={}", serviceId.get());

        String sql ="select to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
                + "info->>'source' as filename "
                + "info->>'digest' as digest "
                + "info->>'serviceId' as service "
                + "from events "
                + "where info->>'serviceId' like '%" + serviceId.get()+ "%' and info->>'state' = 'START' "
                + "order by creationTime asc;";

        List<SearchResult> searchResults = getSearchResults(sql);

        SearchServiceReponse searchServiceReponse = new SearchServiceReponse();
        for (SearchResult searchResult : searchResults) {
            searchServiceReponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
                    searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
        }

        return searchServiceReponse;
    }
}
