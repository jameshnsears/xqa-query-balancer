package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.resources.messagebroker.*;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.jms.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.text.MessageFormat;
import java.util.UUID;
import java.util.Vector;


@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);

    private final String serviceId;
    private final String messageBrokerHost;
    public final Vector<Message> xqueryResponsesFromShards;
    private MessageSender messageSender;
    private ObjectMapper mapper = new ObjectMapper();
    private Message xqueryRequestMessage = null;

    public XQueryResource(MessageBrokerConfiguration messageBrokerConfiguration, String serviceId) {
        synchronized (this) {
            this.serviceId = serviceId;
            this.messageBrokerHost = messageBrokerConfiguration.getHost();
            xqueryResponsesFromShards = new Vector<>();
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) { // json in
        if (xquery.getXqueryRequest().isEmpty())
            throw new WebApplicationException("No XQuery", Response.Status.BAD_REQUEST);

        logger.debug(xquery.toString());





        String correlationId = UUID.randomUUID().toString();
        try {
            synchronized (this) {
                messageSender = new MessageSender(messageBrokerHost);

                sendEventToMessageBroker(
                        new QueryBalancerEvent(serviceId, correlationId,
                                DigestUtils.sha256Hex(MessageLogging.getTextFromMessage(xqueryRequestMessage)), "START"));
            }

            sendXQueryToShards();
            logger.info("" + xqueryResponsesFromShards.size());

            synchronized (this) {
                sendEventToMessageBroker(new QueryBalancerEvent(serviceId, correlationId,
                        DigestUtils.sha256Hex(MessageLogging.getTextFromMessage(xqueryRequestMessage)), "END"));

                messageSender.close();
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        }





        return new XQueryResponse("<some xquery response/>"); // json out
    }

    private void sendEventToMessageBroker(final QueryBalancerEvent ingestBalancerEvent) throws Exception {
        BytesMessage messageSent = messageSender.sendMessage(MessageSender.DestinationType.Queue,
                "xqa.db.amqp.insert_event", UUID.randomUUID().toString(), null, null,
                mapper.writeValueAsString(ingestBalancerEvent), DeliveryMode.PERSISTENT);
        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, messageSent, true));

    }

    private void sendXQueryToShards() throws Exception {
        ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(messageBrokerHost);

        Connection connection = factory.createConnection("admin", "admin");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination sizeDestination = session.createTopic("xqa.shard.sendXqueryToShards");
        Destination sizeReplyToDestination = session.createTemporaryQueue();

        sendXQueryRequest(session, sizeDestination, sizeReplyToDestination);

        shardResponses(session, sizeReplyToDestination);

        session.close();
        connection.close();
    }

    private void shardResponses(Session session, Destination sizeReplyToDestination) throws Exception {
        MessageConsumer messageConsumer = session.createConsumer(sizeReplyToDestination);

        synchronized (this) {
            logger.debug(MessageFormat.format("{0}: shardResponses.START", xqueryRequestMessage.getJMSCorrelationID()));
        }

        Message sizeResponse = messageConsumer.receive(60000);
        while (sizeResponse != null) {
            synchronized (this) {
                logger.debug(MessageLogging.log(MessageLogging.Direction.RECEIVE, sizeResponse, false));
                xqueryResponsesFromShards.add(sizeResponse);
            }
            sizeResponse = messageConsumer.receive(5000);
        }

        synchronized (this) {
            logger.debug(MessageFormat.format("{0}: shardResponses.END; xqueryResponsesFromShards={1}",
                    xqueryRequestMessage.getJMSCorrelationID(), xqueryResponsesFromShards.size()));

            if (xqueryResponsesFromShards.size() == 0) {
                logger.warn(MessageFormat.format("{0}: xqueryResponsesFromShards={1}; subject={2}",
                        xqueryRequestMessage.getJMSCorrelationID(),
                        xqueryResponsesFromShards.size(),
                        xqueryRequestMessage.getJMSType()));
            }
        }

        messageConsumer.close();
    }

    private void sendXQueryRequest(Session session, Destination xqueryDestination, Destination sizeReplyToDestination)
            throws Exception {
        MessageProducer messageProducer = session.createProducer(xqueryDestination);
        BytesMessage sizeRequest;
        synchronized (this) {
            sizeRequest = MessageSender.constructMessage(session, xqueryDestination, xqueryRequestMessage.getJMSCorrelationID(),
                    sizeReplyToDestination, null, null);
            logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, sizeRequest, false));
        }
        messageProducer.send(sizeRequest, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        messageProducer.close();
    }
}
