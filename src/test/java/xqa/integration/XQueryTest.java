package xqa.integration;

import java.io.StringReader;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathConstants;

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
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.integration.fixtures.ShardFixture;

@ExtendWith(ConfigurationParameterResolver.class)
public class XQueryTest extends ShardFixture {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> APPLICATION = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class, ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchTest.class);
    private static DockerClient dockerClient;
    private Client client = ClientBuilder.newClient();

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) throws Exception {
        dockerClient = new DockerClient();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);

        // give containers time to start up
        Thread.sleep(5000);
        APPLICATION.before();
    }

    @AfterAll
    public static void stopContainers(final ConfigurationAccessor configurationAccessor) throws Exception {
        dockerClient.rmContainers(configurationAccessor);
        APPLICATION.after();
    }

    @Test
    public void xquerySize() throws Exception {
        setupStorage(APPLICATION.getConfiguration());
        final XQueryResponse xqueryResponse = client.target("http://0.0.0.0:" + APPLICATION.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        /*
        <xqueryResponse>
            <shard id='abb4c954'>
                2
            </shard>
            <shard id='1211040c'>
                2
            </shard>
        </xqueryResponse>
         */

        LOGGER.info(xqueryResponse.getXqueryResponse());

        Document xmlDocument = documentBuilder
                .parse(new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));
        NodeList nodeList = (NodeList) xPath.compile("/xqueryResponse/shard").evaluate(xmlDocument,
                XPathConstants.NODESET);
        Assertions.assertEquals(2, nodeList.getLength());

        assertShardsCumulativeSize(nodeList);
    }

    private void assertShardsCumulativeSize(NodeList nodeList) {
        int cumulativeSize = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Assertions.assertEquals(false, node.getAttributes().getNamedItem("id").getNodeValue().isEmpty());

            cumulativeSize += Integer.parseInt(node.getFirstChild().getTextContent().replaceAll("\\s", ""));
        }

        Assertions.assertEquals(4, cumulativeSize);
    }

    @Test
    public void xqueryContent() throws Exception {
        final XQueryResponse xqueryResponse = client.target("http://0.0.0.0:" + APPLICATION.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(new XQueryRequest("/chapter/metadataInfo/PSMID"))).readEntity(XQueryResponse.class);

        /*
        <xqueryResponse>
            <shard id='1211040c'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1943_0958_000_0000</PSMID>
            </shard>
            <shard id='abb4c954'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1949_1705_000_0000</PSMID>
            </shard>
        </xqueryResponse>
         */

        LOGGER.info(xqueryResponse.getXqueryResponse());

        Document xmlDocument = documentBuilder
                .parse(new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));
        NodeList nodeList = (NodeList) xPath.compile("/xqueryResponse/shard/PSMID").evaluate(xmlDocument,
                XPathConstants.NODESET);
        Assertions.assertEquals(2, nodeList.getLength());
    }
}
