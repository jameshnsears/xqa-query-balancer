package xqa.resources;

import java.util.List;
import java.util.UUID;
import java.util.Vector;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageBroker.MessageBrokerException;
import xqa.commons.qpid.jms.MessageMaker;
import xqa.resources.messagebroker.MessageBrokerConfiguration;
import xqa.resources.messagebroker.QueryBalancerEvent;

@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(XQueryResource.class);
    private final String serviceId;
    private TemporaryQueue shardReplyToQueue;
    private MessageBroker messageBroker;
    private String auditDestination;
    private String xqueryDestination;
    private int shardResponseTimeout;
    private int shardResponseSecondaryTimeout;

    public XQueryResource(final MessageBrokerConfiguration messageBrokerConfiguration, final String serviceId)
            throws InterruptedException, MessageBroker.MessageBrokerException {
        synchronized (this) {
            this.serviceId = serviceId;

            messageBroker = new MessageBroker(messageBrokerConfiguration.getHost(), messageBrokerConfiguration.getPort(),
                    messageBrokerConfiguration.getUserName(), messageBrokerConfiguration.getPassword(),
                    messageBrokerConfiguration.getRetryAttempts());

            auditDestination = messageBrokerConfiguration.getAuditDestination();
            xqueryDestination = messageBrokerConfiguration.getXqueryDestination();

            shardResponseTimeout = messageBrokerConfiguration.getShardResponseTimeout();
            LOGGER.info(String.format("shardResponseTimeout=%d", shardResponseTimeout));
            shardResponseSecondaryTimeout = messageBrokerConfiguration.getShardResponseSecondaryTimeout();
            LOGGER.info(String.format("shardResponseSecondaryTimeout=%d", shardResponseSecondaryTimeout));
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(final @NotNull @Valid XQueryRequest xquery)
            throws JMSException, JsonProcessingException, MessageBrokerException { // json in
        if (xquery.getXQueryRequest().isEmpty()) {
            throw new WebApplicationException("no xquery", Response.Status.BAD_REQUEST);
        }

        List<Message> shardXQueryResponses = new Vector<>();

        final String correlationId = UUID.randomUUID().toString();

        sendAuditEvent(QueryBalancerEvent.State.START, correlationId, xquery.toString());

        sendXQueryToShards(xquery, correlationId);

        shardXQueryResponses = collectShardXQueryResponses();

        sendAuditEvent(QueryBalancerEvent.State.END, correlationId, xquery.toString());

        return new XQueryResponse(materialiseShardXQueryResponses(shardXQueryResponses)); // json out
    }

    public synchronized void sendAuditEvent(final QueryBalancerEvent.State eventState,
                                            final String correlationId,
                                            final String xquery)
            throws JMSException, JsonProcessingException, MessageBroker.MessageBrokerException {
        final QueryBalancerEvent queryBalancerEvent = new QueryBalancerEvent(serviceId, correlationId,
                DigestUtils.sha256Hex(xquery), eventState);

        final ObjectMapper mapper = new ObjectMapper();

        final Message message = MessageMaker.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createQueue(auditDestination), UUID.randomUUID().toString(),
                mapper.writeValueAsString(queryBalancerEvent));

        messageBroker.sendMessage(message);
    }

    public synchronized void sendXQueryToShards(final @NotNull @Valid XQueryRequest xquery,
                                                final String correlationId)
            throws JMSException, MessageBroker.MessageBrokerException {
        shardReplyToQueue = messageBroker.createTemporaryQueue();

        final Message message = MessageMaker.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createTopic(xqueryDestination), shardReplyToQueue, correlationId,
                xquery.getXQueryRequest());

        messageBroker.sendMessage(message);
    }

    public synchronized List<Message> collectShardXQueryResponses() throws JMSException {
        return messageBroker.receiveMessagesTemporaryQueue(shardReplyToQueue, shardResponseTimeout,
                shardResponseSecondaryTimeout);
    }

    private String materialiseShardXQueryResponses(final List<Message> shardXQueryResponses) throws JMSException {
        final StringBuilder response = new StringBuilder("<xqueryResponse>\n");
        for (final Message message : shardXQueryResponses) {
            response.append(MessageMaker.getBody(message));
        }
        return response.append("</xqueryResponse>").toString();
    }
}
