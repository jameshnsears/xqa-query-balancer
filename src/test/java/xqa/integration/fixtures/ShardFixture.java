package xqa.integration.fixtures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import javax.jms.Message;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xqa.XqaQueryBalancerConfiguration;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMaker;
import xqa.resources.messagebroker.MessageBrokerConfiguration;

public class ShardFixture extends Containerisation {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardFixture.class);
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    protected DocumentBuilder documentBuilder;
    protected XPath xPath = XPathFactory.newInstance().newXPath();
    private MessageBrokerConfiguration messageBrokerConfiguration;
    private MessageBroker messageBroker;

    public ShardFixture() throws ParserConfigurationException {
        documentBuilder = documentBuilderFactory.newDocumentBuilder();
    }

    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("shard").getPath();
    }

    protected void setupStorage(XqaQueryBalancerConfiguration configuration) throws Exception {
        messageBrokerConfiguration = configuration.getMessageBrokerConfiguration();

        messageBroker = new MessageBroker(messageBrokerConfiguration.getHost(), messageBrokerConfiguration.getPort(),
                messageBrokerConfiguration.getUserName(), messageBrokerConfiguration.getPassword(),
                messageBrokerConfiguration.getRetryAttempts());

        populateShards();
        waitForDataToGetInsertedIntoShards();

        messageBroker.close();
    }

    private void waitForDataToGetInsertedIntoShards() throws InterruptedException {
        Thread.sleep(15000);
    }

    private void populateShards() throws IOException {
        try (Stream<Path> filePathStream = Files.walk(Paths.get(getResource()))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        insertFileContentsIntoShard(filePath);
                    } catch (Exception exception) {
                        LOGGER.error(exception.getMessage());
                    }
                }
            });
        }
    }

    private void insertFileContentsIntoShard(Path filePath) throws Exception {
        LOGGER.debug(filePath.toString());
        Message message = MessageMaker.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createQueue(messageBrokerConfiguration.getIngestDestination()),
                UUID.randomUUID().toString(), filePath.toString(),
                FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8));

        messageBroker.sendMessage(message);
    }
}
