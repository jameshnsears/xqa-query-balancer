package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.resources.messagebroker.*;

import javax.jms.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.Vector;


@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);

    private final MessageBrokerConfiguration messageBrokerConfiguration;

    private final String serviceId;
    private ObjectMapper mapper = new ObjectMapper();

    public XQueryResource(MessageBrokerConfiguration messageBrokerConfiguration, String serviceId) {
        synchronized (this) {
            this.messageBrokerConfiguration = messageBrokerConfiguration;

            this.serviceId = serviceId;
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) { // json in
        if (xquery.getXqueryRequest().isEmpty())
            throw new WebApplicationException("No XQuery", Response.Status.BAD_REQUEST);

        logger.info(xquery.toString());

        String correlationId = UUID.randomUUID().toString();

        try {
            MessageSender messageSender = new MessageSender(this.messageBrokerConfiguration.getHost());

            synchronized (this) {
                sendAuditEvent(
                        messageSender,
                        new QueryBalancerEvent(
                                serviceId,
                                correlationId,
                                DigestUtils.sha256Hex(xquery.toString()),
                                "START"));
            }

            ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(this.messageBrokerConfiguration.getHost());

            Connection connection = factory.createConnection(this.messageBrokerConfiguration.getUserName(),
                    this.messageBrokerConfiguration.getPassword());
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            BytesMessage xqueryMessage = sendXQueryRequest(session, correlationId, xquery.toString());

            Vector<Message> shardResponses = shardResponses(session, xqueryMessage);

            session.close();
            connection.close();

            synchronized (this) {
                sendAuditEvent(
                        messageSender,
                        new QueryBalancerEvent(
                            serviceId,
                            correlationId,
                            DigestUtils.sha256Hex(xquery.toString()),
                            "END"));

                messageSender.close();
            }
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            exception.printStackTrace();
            System.exit(1);
        }

        return new XQueryResponse("<some xquery response/>"); // json out
    }


    private void sendAuditEvent(MessageSender messageSender, final QueryBalancerEvent queryBalancerEvent) throws Exception {
        BytesMessage messageSent = messageSender.sendMessage(
                MessageSender.DestinationType.Queue,
                this.messageBrokerConfiguration.getAuditDestination(),
                UUID.randomUUID().toString(),
                null,
                null,
                mapper.writeValueAsString(queryBalancerEvent), DeliveryMode.PERSISTENT);

        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, messageSent, true));
    }

    private Vector<Message> shardResponses(Session session, BytesMessage xqueryMessage) throws Exception {
        MessageConsumer messageConsumer = session.createConsumer(xqueryMessage.getJMSReplyTo());

//        synchronized (this) {
//            logger.debug(MessageFormat.format("{0}: shardResponses.START", correlationId));
//        }


        Vector<Message> xqueryResponsesFromShards = new Vector<>();
        try {
            Thread.sleep(4000);
//
//            Message sizeResponse = messageConsumer.receive(60000);
//            while (sizeResponse != null) {
//                synchronized (this) {
//                    logger.debug(MessageLogging.log(MessageLogging.Direction.RECEIVE, sizeResponse, false));
//                    xqueryResponsesFromShards.add(sizeResponse);
//                }
//                sizeResponse = messageConsumer.receive(5000);
//            }

//        synchronized (this) {
//            logger.debug(MessageFormat.format("{0}: shardResponses.END; xqueryResponsesFromShards={1}",
//                    correlationId, xqueryResponsesFromShards.size()));
//
//            if (xqueryResponsesFromShards.size() == 0) {
//                logger.warn(MessageFormat.format("{0}: xqueryResponsesFromShards={1}; subject={2}",
//                        correlationId,
//                        xqueryResponsesFromShards.size(),
//                        xqueryRequestMessage.getJMSType()));
//            }
//        }

            messageConsumer.close();
        }
        catch (Exception e) {
            logger.error(e.getMessage());
        }

        return xqueryResponsesFromShards;
    }

    private BytesMessage sendXQueryRequest(Session session,
                                   String correlationId,
                                   String body)
            throws Exception {
        Destination xqueryDestination = session.createTopic(this.messageBrokerConfiguration.getXqueryDestination());

        MessageProducer messageProducer = session.createProducer(xqueryDestination);
        BytesMessage xqueryMessage;
        synchronized (this) {
            xqueryMessage = MessageSender.constructMessage(
                    session,
                    xqueryDestination,
                    correlationId,
                    session.createTemporaryQueue(),
                    null,
                    body);
            logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, xqueryMessage, false));
        }
        messageProducer.send(xqueryMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        messageProducer.close();

        return xqueryMessage;
    }
}
