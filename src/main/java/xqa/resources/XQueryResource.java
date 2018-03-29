package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.qpid.jms.message.JmsBytesMessage;
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
import java.util.concurrent.ThreadPoolExecutor;




@Path("/xquery")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class XQueryResource {
    private static final Logger logger = LoggerFactory.getLogger(XQueryResource.class);
    private ThreadPoolExecutor ingestPoolExecutor;

    private final String serviceId;
    private final String messageBrokerHost;
    public final Vector<Message> shardSizeResponses;
    private MessageSender messageSender;
    private ObjectMapper mapper = new ObjectMapper();
    private Message ingestMessage = null;

    public XQueryResource(MessageBrokerConfiguration messageBrokerConfiguration, String serviceId) {
        synchronized (this) {
            this.serviceId = serviceId;
            this.messageBrokerHost = messageBrokerConfiguration.getHost();
            shardSizeResponses = new Vector<>();
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(@NotNull @Valid XQueryRequest xquery) { // json in
        if (xquery.getXqueryRequest().isEmpty())
            throw new WebApplicationException("No XQuery", Response.Status.BAD_REQUEST);

        logger.debug(xquery.toString());

        String correlationId = UUID.randomUUID().toString();


//        try {
//            synchronized (this) {
//                messageSender = new MessageSender(messageBrokerHost);
//
//                sendEventToMessageBroker(
//                        new QueryBalancerEvent(serviceId, correlationId,
//                                DigestUtils.sha256Hex(MessageLogging.getTextFromMessage(ingestMessage)), "START"));
//            }
//
//            size();
//            Message smallestShard = smallestShard();
//            if (smallestShard != null) {
//                insert(smallestShard, MessageLogging.getTextFromMessage(ingestMessage));
//            }
//
//            synchronized (this) {
//                sendEventToMessageBroker(new QueryBalancerEvent(serviceId, correlationId,
//                        DigestUtils.sha256Hex(MessageLogging.getTextFromMessage(ingestMessage)), "END"));
//
//                messageSender.close();
//            }
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//            exception.printStackTrace();
//            System.exit(1);
//        }



        return new XQueryResponse("<some xquery response/>"); // json out
    }

    private void sendEventToMessageBroker(final QueryBalancerEvent ingestBalancerEvent) throws Exception {
        BytesMessage messageSent = messageSender.sendMessage(MessageSender.DestinationType.Queue,
                "xqa.db.amqp.insert_event", UUID.randomUUID().toString(), null, null,
                mapper.writeValueAsString(ingestBalancerEvent), DeliveryMode.PERSISTENT);
        logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, messageSent, true));

    }

    private void size() throws Exception {
        ConnectionFactory factory = MessageBrokerConnectionFactory.messageBroker(messageBrokerHost);

        Connection connection = factory.createConnection("admin", "admin");
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Destination sizeDestination = session.createTopic("xqa.shard.size");
        Destination sizeReplyToDestination = session.createTemporaryQueue();

        sendSizeRequest(session, sizeDestination, sizeReplyToDestination);

        getSizeResponses(session, sizeReplyToDestination);

        session.close();
        connection.close();
    }

    private void getSizeResponses(Session session, Destination sizeReplyToDestination) throws Exception {
        MessageConsumer messageConsumer = session.createConsumer(sizeReplyToDestination);

        synchronized (this) {
            logger.debug(MessageFormat.format("{0}: getSizeResponses.START", ingestMessage.getJMSCorrelationID()));
        }

        Message sizeResponse = messageConsumer.receive(60000);
        while (sizeResponse != null) {
            synchronized (this) {
                logger.debug(MessageLogging.log(MessageLogging.Direction.RECEIVE, sizeResponse, false));
                shardSizeResponses.add(sizeResponse);
            }
            sizeResponse = messageConsumer.receive(5000);
        }

        synchronized (this) {
            logger.debug(MessageFormat.format("{0}: getSizeResponses.END; shardSizeResponses={1}",
                    ingestMessage.getJMSCorrelationID(), shardSizeResponses.size()));

            if (shardSizeResponses.size() == 0) {
                logger.warn(MessageFormat.format("{0}: shardSizeResponses={1}; subject={2}",
                        ingestMessage.getJMSCorrelationID(),
                        shardSizeResponses.size(),
                        ingestMessage.getJMSType()));
            }
        }

        messageConsumer.close();
    }

    private void sendSizeRequest(Session session, Destination sizeDestination, Destination sizeReplyToDestination)
            throws Exception {
        MessageProducer messageProducer = session.createProducer(sizeDestination);
        BytesMessage sizeRequest;
        synchronized (this) {
            sizeRequest = MessageSender.constructMessage(session, sizeDestination, ingestMessage.getJMSCorrelationID(),
                    sizeReplyToDestination, null, null);
            logger.debug(MessageLogging.log(MessageLogging.Direction.SEND, sizeRequest, false));
        }
        messageProducer.send(sizeRequest, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY,
                Message.DEFAULT_TIME_TO_LIVE);
        messageProducer.close();
    }

    public Message smallestShard() throws Exception {
        Message smallestShard = null;

        if (shardSizeResponses.size() > 0) {
            smallestShard = shardSizeResponses.get(0);

            for (Message currentShard : shardSizeResponses) {
                String textFromSmallestShard = MessageLogging.getTextFromMessage(smallestShard);
                String textFromCurrentShard = MessageLogging.getTextFromMessage(currentShard);
                if (Integer.valueOf(textFromSmallestShard) > Integer.valueOf(textFromCurrentShard)) {
                    smallestShard = currentShard;
                }
            }
        }

        return smallestShard;
    }

    private void insert(Message smallestShard, String text) throws Exception {
        String correlationID;
        String subject;
        synchronized (this) {
            correlationID = this.ingestMessage.getJMSCorrelationID();
            JmsBytesMessage jmsBytesMessage = (JmsBytesMessage) this.ingestMessage;
            subject = jmsBytesMessage.getFacade().getType();
        }

        MessageSender messageSender = new MessageSender(messageBrokerHost);
        BytesMessage messageSent = messageSender.sendMessage(MessageSender.DestinationType.Queue,
                smallestShard.getJMSReplyTo().toString(), correlationID, null, subject, text, DeliveryMode.PERSISTENT);
        logger.info(MessageLogging.log(MessageLogging.Direction.SEND, messageSent, true));

        messageSender.close();
    }
}
