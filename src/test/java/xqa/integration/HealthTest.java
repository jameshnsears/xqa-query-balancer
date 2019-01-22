package xqa.integration;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.integration.fixtures.Containerisation;

@ExtendWith(ConfigurationParameterResolver.class)
public class HealthTest extends Containerisation {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> APPLICATION = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class, ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private DockerClient dockerClient;

    @BeforeEach
    public void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient = new DockerClient();

        final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);
        APPLICATION.before();
    }

    @AfterEach
    public void stopcontainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient.rmContainers(configurationAccessor);
        APPLICATION.after();
    }

    @Test
    public void queryBalancerHealth() throws IOException {
        final Client client = ClientBuilder.newClient();

        final Response healthCheck = client.target("http://0.0.0.0:" + APPLICATION.getAdminPort() + "/healthcheck")
                .request("text/plain").get();

        Assertions.assertEquals(200, healthCheck.getStatus());
    }
}
