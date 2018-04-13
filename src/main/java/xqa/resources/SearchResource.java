package xqa.resources;

import java.util.List;
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
  private final Jdbi jdbi;

  public SearchResource(Jdbi jdbi) {
    synchronized (this) {
      this.jdbi = jdbi;
    }
  }

  @GET
  @Timed
  @Path("/{searchString : .+}")
  public synchronized SearchResponse subject(
      @PathParam("searchString") Optional<String> searchString) {
    logger.debug("searchString={}", searchString.orElse("*"));

    List<SearchResult> searchResults = jdbi.withHandle(handle -> {
      String sql = "select distinct to_timestamp( (info->>'creationTime')::double precision / 1000) as creationTime, "
          + "info->>'serviceId' as serviceId, " + "info->>'source' as subject, "
          + "info->>'digest' as digest " + "from events " + "where (info->>'serviceId' like '%"
          + searchString.get() + "%' " + "or info->>'source' like '%" + searchString.get() + "%' "
          + "or info->>'digest' like '%" + searchString.get() + "%')"
          + "and info->>'state' = 'START'"
          + "order by to_timestamp( (info->>'creationTime')::double precision / 1000) asc;";
      logger.info(sql);
      return handle.createQuery(sql).map((rs, ctx) -> new SearchResult(rs.getString("creationTime"),
          rs.getString("serviceId"), rs.getString("subject"), rs.getString("digest"))).list();
    });

    if (searchResults.isEmpty()) {
      throw new WebApplicationException("No Search Criteria", Response.Status.BAD_REQUEST);
    }

    SearchResponse searchResponse = new SearchResponse();
    for (SearchResult searchResult : searchResults) {
      searchResponse.getSearchResponse().add(new SearchResult(searchResult.getCreationTime(),
          searchResult.getServiceId(), searchResult.getSubject(), searchResult.getdigest()));
    }

    return searchResponse;
  }
}
