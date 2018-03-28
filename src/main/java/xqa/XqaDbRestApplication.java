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
import xqa.health.MessageBrokerHealthCheck;
import xqa.resources.SearchResource;
import xqa.resources.XQueryResource;

public class XqaDbRestApplication extends Application<XqaDbRestConfiguration> {
    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);

    public static void main(String[] args) throws Exception {
        new XqaDbRestApplication().run(args);
    }

    @Override
    public String getName() {
        return "xqa-db-rest";
    }

    @Override
    public void initialize(Bootstrap<XqaDbRestConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(XqaDbRestConfiguration configuration, Environment environment) {
        environment.healthChecks().register("MessageBrokerHealthCheck", new MessageBrokerHealthCheck());

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        environment.jersey().register(new SearchResource(jdbi));

        environment.jersey().register(new XQueryResource(jdbi));
    }
}
