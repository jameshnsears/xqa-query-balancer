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

import xqa.api.core.SearchPOJO;
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

        /*
        -- confirm where content distributed
select info->>'serviceId' as serviceid, count(distinct(info->>'correlationId')) as items
from events
group by serviceid


-- it's possible for shard to START / END before ingestbalancer END'd
select info->>'creationTime' as creationtime,
info->>'serviceId' as serviceid,
info->>'size' as size,
info->>'poolSize' as poolsize,
info->>'state' as state,
info->>'correlationId' as correlationid,
info->>'digest' as digest
from events
where info->>'correlationId' in (
select distinct(info->>'correlationId') as correlationid
from events
where info->>'source' like '%/xml/SP-MAIN-245-m0130-cm.xml%'
)
order by events.when asc;


         */


        List<SearchPOJO> users = jdbi.withHandle(handle -> {
            String sql = "select info->>'creationTime' as creationTime,\n" +
                    "       info->>'serviceId' as serviceId,\n" +
                    "       info->>'source' as subject,\n" +
                    "       info->>'digest' as digest\n" +
                    "from events\n" +
                    "order by events.when asc;";

            return handle.createQuery(sql)
                    .map((rs, ctx) -> new SearchPOJO(rs.getString("creationTime"), rs.getString("serviceId"), rs.getString("subject"), rs.getString("digest")))
                    .list();
        });

        searchResponse.getSearchResponse().add(new SearchResult("2018-03-16 17:52:23.259682", "ingest/02bd02c2", "DBER-1923-0416.xml", "aa84010b"));

        return searchResponse;
    }
}
