package xqa.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageBroker.MessageBrokerException;
import xqa.commons.qpid.jms.MessageMaker;
import xqa.resources.messagebroker.MessageBrokerConfiguration;
import xqa.resources.messagebroker.QueryBalancerEvent;

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
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

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
    private DocumentBuilderFactory documentBuilderFactory;
    private TransformerFactory factory;
    private Transformer transformer;

    public XQueryResource(final MessageBrokerConfiguration messageBrokerConfiguration, final String serviceId)
            throws InterruptedException,
            MessageBroker.MessageBrokerException,
            TransformerConfigurationException, ParserConfigurationException {
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

            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilderFactory.setXIncludeAware(false);
            documentBuilderFactory.setExpandEntityReferences(false);

            factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
    }

    @POST
    @Timed
    public XQueryResponse xquery(final @NotNull @Valid XQueryRequest xquery)
            throws JMSException, IOException, MessageBrokerException, TransformerException { // json in
        if (xquery.getXQueryRequest().isEmpty()) {
            throw new WebApplicationException("no xquery", Response.Status.BAD_REQUEST);
        }

        List<Message> shardXQueryResponses;

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

    private String materialiseShardXQueryResponses(final List<Message> shardXQueryResponses)
            throws JMSException, TransformerException {
        final StringBuilder response = new StringBuilder();
        response.append("<xqueryResponse>\n");

        for (final Message message : shardXQueryResponses) {
            response.append(MessageMaker.getBody(message));
            response.append('\n');
        }
        response.append("</xqueryResponse>");

        return prettyPrintXml(response.toString());
    }

    private String prettyPrintXml(final String xml)
            throws TransformerException {
        try {
            StreamSource streamSource = new StreamSource(new StringReader(xml.replace("\n", "")));
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            synchronized (this) {
                transformer.transform(streamSource, streamResult);
            }
            return stringWriter.toString();
        } catch (TransformerException exception) {
            LOGGER.error(exception.getMessage());
            throw exception;
        }
    }
}
