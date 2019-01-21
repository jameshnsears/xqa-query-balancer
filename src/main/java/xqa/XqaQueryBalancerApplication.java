package xqa;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.health.QueryBalancerHealthCheck;
import xqa.resources.SearchResource;
import xqa.resources.XQueryResource;

import java.util.UUID;

public class XqaQueryBalancerApplication extends Application<XqaQueryBalancerConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);
    private final String serviceId;

    public XqaQueryBalancerApplication() {
        serviceId = this.getClass().getSimpleName().toLowerCase() + "/"
                + UUID.randomUUID().toString().split("-")[0];
        logger.info(serviceId);
    }

    public static void main(String[] args) throws Exception {
        new XqaQueryBalancerApplication().run(args);
    }

    @Override
    public String getName() {
        return "xqa-query-balancer";
    }

    @Override
    public void initialize(Bootstrap<XqaQueryBalancerConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(XqaQueryBalancerConfiguration configuration, Environment environment)
            throws Exception {

        environment.healthChecks().register("QueryBalancerHealthCheck", new QueryBalancerHealthCheck());

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(),
                "postgresql");

        environment.jersey()
                .register(new SearchResource(jdbi));
        environment.jersey()
                .register(new XQueryResource(configuration.getMessageBrokerConfiguration(), serviceId));
    }
}
