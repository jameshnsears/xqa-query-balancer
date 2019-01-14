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
import org.assertj.core.api.Assertions;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import xqa.integration.fixtures.ShardFixture;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.api.xquery.XQueryRequest;
import xqa.api.xquery.XQueryResponse;

import javax.ws.rs.client.Client;
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

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    private static DockerClient dockerClient;

    @BeforeAll
    public static void startContainers(final ConfigurationAccessor configurationAccessor) {
        dockerClient = new DockerClient();

        LoggerContext loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
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

        Client client = JerseyClientBuilder.createClient();

        final XQueryResponse xqueryResponse = client
                .property(ClientProperties.READ_TIMEOUT, 10000)
                .target("http://0.0.0.0:" + application.getLocalPort() + "/xquery")
                .request(MediaType.APPLICATION_JSON_TYPE).post(Entity.json(new XQueryRequest("count(/)")))
                .readEntity(XQueryResponse.class);

        logger.info(xqueryResponse.getXqueryResponse());

        Document xmlDocument = documentBuilder.parse(
                new InputSource(new StringReader(xqueryResponse.getXqueryResponse())));

        NodeList nodeList = (NodeList) xPath.compile(
                "/xqueryResponse/shard").evaluate(xmlDocument,
                XPathConstants.NODESET);

        org.junit.jupiter.api.Assertions.assertEquals(2, nodeList.getLength());

        int cumulativeSize = 0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
                    Assertions.assertThat(node.getAttributes().getNamedItem("id").getNodeValue()).isNotEmpty();

            cumulativeSize += Integer.parseInt(node.getFirstChild().getTextContent().replaceAll("\\s",""));
        }

        org.junit.jupiter.api.Assertions.assertEquals(4, cumulativeSize);
    }
}
