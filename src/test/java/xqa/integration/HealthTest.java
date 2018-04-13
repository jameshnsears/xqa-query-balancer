package xqa.integration;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.ClassRule;
import org.junit.Test;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthTest {
    @ClassRule
    public static final DropwizardAppRule<XqaQueryBalancerConfiguration> application = new DropwizardAppRule<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));

    @Test
    public void queryBalancerHealth() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(new URL("http://127.0.0.1:" + application.getAdminPort() + "/healthcheck")).build();

        Response response = client.newCall(request).execute();
        assertThat(response.code() == 200);

    }
}
