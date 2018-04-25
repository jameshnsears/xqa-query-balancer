package xqa.integration.fixtures;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaQueryBalancerConfiguration;
import xqa.commons.qpid.jms.MessageBroker;
import xqa.commons.qpid.jms.MessageMaker;
import xqa.resources.messagebroker.MessageBrokerConfiguration;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TemporaryQueue;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ShardFixture {
    private static final Logger logger = LoggerFactory.getLogger(ShardFixture.class);
    MessageBrokerConfiguration messageBrokerConfiguration;
    private MessageBroker messageBroker;

    public String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("shard")
                .getPath();
    }

    public void setupStorage(XqaQueryBalancerConfiguration configuration) throws Exception {
        messageBrokerConfiguration = configuration.getMessageBrokerConfiguration();

        messageBroker = new MessageBroker(messageBrokerConfiguration.getHost(),
                messageBrokerConfiguration.getPort(), messageBrokerConfiguration.getUserName(),
                messageBrokerConfiguration.getPassword(), messageBrokerConfiguration.getRetryAttempts());

        populate(findInsertDestination());

        messageBroker.close();
    }

    private String findInsertDestination() throws JMSException, UnsupportedEncodingException {
        TemporaryQueue replyTo = messageBroker.createTemporaryQueue();

        messageBroker.sendMessage(MessageMaker.createMessage(messageBroker.getSession(),
                messageBroker.getSession().createTopic(messageBrokerConfiguration.getSizeDestination()),
                replyTo,
                UUID.randomUUID().toString(),
                ""));

        List<Message> messages = messageBroker.receiveMessagesTemporaryQueue(replyTo, 2000);
        return messages.get(0).getJMSReplyTo().toString();
    }

    private void populate(String insertDestination) throws IOException {
        try (Stream<Path> filePathStream = Files.walk(Paths.get(getResource()))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        insertFileContentsIntoShard(insertDestination, filePath);
                    } catch (Exception exception) {
                        logger.error(exception.getMessage());
                    }
                }
            });
        }
    }

    private void insertFileContentsIntoShard(String insertDestination, Path filePath) throws Exception {
        Message message = MessageMaker.createMessage(
                messageBroker.getSession(),
                messageBroker.getSession().createQueue(insertDestination),
                UUID.randomUUID().toString(),
                filePath.toString(),
                FileUtils.readFileToString(filePath.toFile(), StandardCharsets.UTF_8));

        messageBroker.sendMessage(message);
    }
}
