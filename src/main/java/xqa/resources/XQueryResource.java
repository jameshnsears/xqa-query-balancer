package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);
    private Jdbi jdbi;

    public XQueryResource(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) { // json in
        logger.debug(xquery.toString());

        XQueryResponse response = new XQueryResponse("<a>b</a>");

        return response; // json out
    }
}
