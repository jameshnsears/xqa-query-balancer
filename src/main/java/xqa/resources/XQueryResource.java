package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMaker;
import xqa.resources.messagebroker.MessageBrokerConfiguration;
import xqa.resources.messagebroker.QueryBalancerEvent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);

    private final String serviceId;
    private TemporaryQueue shardReplyToQueue;
    private MessageBroker messageBroker;
    private String auditDestination;
    private String xqueryDestination;

    public XQueryResource(MessageBrokerConfiguration messageBrokerConfiguration, String serviceId)
            throws Exception {
        synchronized (this) {
            this.serviceId = serviceId;

            messageBroker = new MessageBroker(messageBrokerConfiguration.getHost(),
                    messageBrokerConfiguration.getPort(), messageBrokerConfiguration.getUserName(),
                    messageBrokerConfiguration.getPassword(), messageBrokerConfiguration.getRetryAttempts());

            auditDestination = messageBrokerConfiguration.getAuditDestination();
            xqueryDestination = messageBrokerConfiguration.getXqueryDestination();
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) throws Exception { // json in
        if (xquery.getXQueryRequest().isEmpty()) {
            throw new WebApplicationException("no xquery", Response.Status.BAD_REQUEST);
        }

        logger.info(xquery.toString());
        List<Message> shardXQueryResponses = null;

        try {
            String correlationId = UUID.randomUUID().toString();

            sendAuditEvent(QueryBalancerEvent.State.START, correlationId, xquery.toString());

            sendXQueryToShards(xquery, correlationId);

            shardXQueryResponses = collectShardXQueryResponses();

            sendAuditEvent(QueryBalancerEvent.State.END, correlationId, xquery.toString());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        } finally {
            synchronized (this) {
                messageBroker.close();
            }
        }

        return new XQueryResponse(materialiseShardXQueryResponses(shardXQueryResponses)); // json out
    }

    private synchronized void sendAuditEvent(QueryBalancerEvent.State eventState,
                                             String correlationId, String xquery) throws Exception {
        QueryBalancerEvent queryBalancerEvent = new QueryBalancerEvent(serviceId, correlationId,
                DigestUtils.sha256Hex(xquery), eventState);

        ObjectMapper mapper = new ObjectMapper();

        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                messageBroker.getSession().createQueue(auditDestination),
                UUID.randomUUID().toString(),
                mapper.writeValueAsString(queryBalancerEvent));

        messageBroker.sendMessage(message);
    }

    private synchronized void sendXQueryToShards(@NotNull @Valid XQueryRequest xquery,
                                                 String correlationId) throws Exception {
        shardReplyToQueue = messageBroker.createTemporaryQueue();

        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                messageBroker.getSession().createTopic(xqueryDestination),
                shardReplyToQueue,
                correlationId,
                xquery.getXQueryRequest());

        messageBroker.sendMessage(message);
    }

    private synchronized List<Message> collectShardXQueryResponses() throws Exception {
        return messageBroker.receiveMessagesTemporaryQueue(shardReplyToQueue, 60000);
    }

    private synchronized String materialiseShardXQueryResponses(List<Message> shardXQueryResponses)
            throws UnsupportedEncodingException, JMSException {
        String response = "<xqueryResponse>\n";
        for (Message message: shardXQueryResponses) {
            response += MessageMaker.getBody(message) + "\n";
        }
        return response += "</xqueryResponse>";
    }
}
