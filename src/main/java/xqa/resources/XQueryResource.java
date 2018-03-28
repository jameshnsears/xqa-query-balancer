package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        if (xquery.getXqueryRequest().isEmpty())
            throw new WebApplicationException("No XQuery", Response.Status.BAD_REQUEST);

        logger.debug(xquery.toString());

        XQueryResponse response = new XQueryResponse("<some xquery response/>");

        return response; // json out
    }
}
