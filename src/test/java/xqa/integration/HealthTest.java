package xqa.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;

import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;
import xqa.integration.fixtures.Containerisation;

@ExtendWith(ConfigurationParameterResolver.class)
public class HealthTest extends Containerisation {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> application = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private DockerClient dockerClient;

    @BeforeEach
    public void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient = new DockerClient();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("com.github.jameshnsears.docker.DockerClient");
        ((ch.qos.logback.classic.Logger) rootLogger).setLevel(Level.OFF);

        dockerClient.pull(configurationAccessor.images());
        dockerClient.startContainers(configurationAccessor);

        application.before();
    }

    @AfterEach
    public void stopcontainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient.rmContainers(configurationAccessor);

        application.after();
    }

    @Test
    public void queryBalancerHealth() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(new URL("http://127.0.0.1:" + application.getAdminPort() + "/healthcheck")).build();

        Response response = client.newCall(request).execute();
        assertThat(response.code() == 200);

    }
}
