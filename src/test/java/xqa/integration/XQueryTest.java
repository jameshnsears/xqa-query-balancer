package xqa.integration;

import java.io.IOException;
import java.io.StringReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.xml.sax.SAXException;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.integration.fixtures.ShardFixture;

@ExtendWith(ConfigurationParameterResolver.class)
public class XQueryTest extends ShardFixture {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> APPLICATION = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class, ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private static DockerClient dockerClient;
    private Client client = ClientBuilder.newClient();
    
    public XQueryTest() throws ParserConfigurationException {
        super();
    }

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException, InterruptedException {
        dockerClient = new DockerClient();

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);

        // give containers time to start up
        Thread.sleep(5000);
        APPLICATION.before();
    }

    @AfterAll
    public static void stopContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient.rmContainers(configurationAccessor);
        APPLICATION.after();
    }

    @Test
    public void xquerySize() throws Exception {
        setupStorage(APPLICATION.getConfiguration());
        final XQueryResponse xqueryResponse = client.target("http://0.0.0.0:" + APPLICATION.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        LOGGER.info(xqueryResponse.getXqueryResponse());

        final Document xmlDocument = documentBuilder
                .parse(new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));
        final NodeList nodeList = (NodeList) xPath.compile("/xqueryResponse/shard").evaluate(xmlDocument,
                XPathConstants.NODESET);
        Assertions.assertEquals(2, nodeList.getLength());

        assertShardsCumulativeSize(nodeList);
    }

    private void assertShardsCumulativeSize(final NodeList nodeList) {
        int cumulativeSize = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            Assertions.assertEquals(false, node.getAttributes().getNamedItem("id").getNodeValue().isEmpty());

            cumulativeSize += Integer.parseInt(node.getFirstChild().getTextContent().replaceAll("\\s", ""));
        }

        Assertions.assertEquals(4, cumulativeSize);
    }

    @Test
    public void xqueryContent() throws IOException, SAXException, XPathExpressionException {
        final XQueryResponse xqueryResponse = client.target("http://0.0.0.0:" + APPLICATION.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(new XQueryRequest("/chapter/metadataInfo/PSMID"))).readEntity(XQueryResponse.class);

        LOGGER.info(xqueryResponse.getXqueryResponse().toString());

        final Document xmlDocument = documentBuilder
                .parse(new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));
        final NodeList nodeList = (NodeList) xPath.compile("/xqueryResponse/shard/PSMID").evaluate(xmlDocument,
                XPathConstants.NODESET);
        Assertions.assertEquals(2, nodeList.getLength());
    }
}
