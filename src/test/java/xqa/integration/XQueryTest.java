package xqa.integration;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
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
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;
import xqa.integration.fixtures.ShardFixture;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathConstants;
import java.io.StringReader;

@ExtendWith(ConfigurationParameterResolver.class)
public class XQueryTest extends ShardFixture {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> application = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private static final Logger logger = LoggerFactory.getLogger(SearchTest.class);
    private static DockerClient dockerClient;
    private Client client = ClientBuilder.newClient();
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) {
        dockerClient = new DockerClient();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        try {
            dockerClient.pull(configurationAccessor.images());
            dockerClient.startContainers(configurationAccessor);

            // give containers time to start up
            Thread.sleep(5000);
            application.before();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void stopContainers(final ConfigurationAccessor configurationAccessor) {
        try {
            dockerClient.rmContainers(configurationAccessor);
            application.after();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void xquerySize() throws Exception {
        setupStorage(application.getConfiguration());
        final XQueryResponse xqueryResponse = client
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        /*
        <xqueryResponse>
            <shard id='8d356dbe'>
            2
            </shard>
            <shard id='5cb35499'>
            2
            </shard>
        </xqueryResponse>
         */

        logger.info(xqueryResponse.getXqueryResponse());


        final XQueryResponse x = client
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("/chapter/metadataInfo/PSMID")))
                .readEntity(XQueryResponse.class);

        /*
        <xqueryResponse>
            <shard id='e9496a00'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1949_1705_000_0000</PSMID>
            </shard>
            <shard id='2c24cfc9'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1943_0958_000_0000</PSMID>
            </shard>
        </xqueryResponse>
         */

        logger.info(x.getXqueryResponse());



//        Document xmlDocument = documentBuilder.parse(
//                new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));
//
//        NodeList nodeList = (NodeList) xPath.compile(
//                "/xqueryResponse/shard").evaluate(xmlDocument,
//                XPathConstants.NODESET);
//
//        Assertions.assertEquals(2, nodeList.getLength());
//
//        assertShardsCumulativeSize(nodeList);
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

    //@Test
    public void xqueryContent() throws Exception {
        final XQueryResponse xqueryResponse = client
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("/chapter/metadataInfo/PSMID")))
                .readEntity(XQueryResponse.class);

        /*
        <xqueryResponse>
            <shard id='e9496a00'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1949_1705_000_0000</PSMID>
            </shard>
            <shard id='2c24cfc9'>
                <PSMID xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">cho_meet_1943_0958_000_0000</PSMID>
            </shard>
        </xqueryResponse>
         */

        logger.info(xqueryResponse.getXqueryResponse());

    }

}
