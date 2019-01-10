package xqa.integration;

import com.github.jameshnsears.configuration.ConfigurationAccessor;
import com.github.jameshnsears.configuration.ConfigurationParameterResolver;
import com.github.jameshnsears.docker.DockerClient;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ConfigurationParameterResolver.class)
public class HealthTest {
    private static final DropwizardTestSupport<XqaQueryBalancerConfiguration> application = new DropwizardTestSupport<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    private DockerClient dockerClient;

    @BeforeEach
    public void startContainers(final ConfigurationAccessor configurationAccessor) throws IOException {
        dockerClient = new DockerClient();
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
