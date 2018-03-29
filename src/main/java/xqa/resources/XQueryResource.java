package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.resources.messagebroker.MessageBrokerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.jms.Message;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;


@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);
    private ThreadPoolExecutor ingestPoolExecutor;
    private String servicdId;

    public XQueryResource(MessageBrokerConfiguration messageBrokerConfiguration, String servicdId) {
//
//        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("InserterThread-%d").setDaemon(true)
//                .build();
//
//        ingestPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Integer.valueOf(poolSize),
//                threadFactory);
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) { // json in
        if (xquery.getXqueryRequest().isEmpty())
            throw new WebApplicationException("No XQuery", Response.Status.BAD_REQUEST);

        logger.debug(xquery.toString());

        /*
        TODO

        = send msg (with Xquery) to topic with # replyTo
            = each shard processes msg and sends back response to replyTo
        = wait and then collate / materialise reponses
        = receive json back
         */

        String serviceId = this.getClass().getSimpleName().toLowerCase() + "/" + UUID.randomUUID().toString().split("-")[0];
        String messageBrokerHost;
        String poolSize;
        Message message;
        //ingestPoolExecutor.execute(new InserterThread(serviceId, messageBrokerHost, poolSize, message));

        return new XQueryResponse("<some xquery response/>"); // json out
    }
}
