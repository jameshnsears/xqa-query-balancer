package xqa.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;

import xqa.api.status.StatusResponse;
import xqa.api.status.StatusResult;

@Path("/status")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StatusResource {
    private static final Logger logger = LoggerFactory.getLogger(StatusResource.class);

    @GET
    @Timed
    public StatusResponse status() {
        StatusResponse statusResponse = new StatusResponse();

        statusResponse.getSearchResponse()
                .add(new StatusResult(Boolean.TRUE, "xqa-ingest-balancer-uuid-1", 40, "2018010914020", Boolean.TRUE));

        statusResponse.getSearchResponse()
                .add(new StatusResult(Boolean.FALSE, "xqa-shard-uuid-1", 21, "20180109140201", Boolean.TRUE));

        statusResponse.getSearchResponse()
                .add(new StatusResult(Boolean.FALSE, "xqa-shard-uuid-2", 19, "20180109140203", Boolean.FALSE));

        return statusResponse; // json out
    }
}
