package xqa;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import xqa.health.MessageBrokerHealthCheck;
import xqa.resources.SearchResource;
import xqa.resources.StatusResource;
import xqa.resources.XQueryResource;

public class XqaRestApplication extends Application<XqaRestConfiguration> {
    public static void main(String[] args) throws Exception {
        new XqaRestApplication().run(args);
    }

    @Override
    public String getName() {
        return "xqa-db-rest";
    }

    @Override
    public void initialize(Bootstrap<XqaRestConfiguration> bootstrap) {
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    }

    @Override
    public void run(XqaRestConfiguration configuration, Environment environment) {
        environment.healthChecks().register("MessageBrokerHealthCheck", new MessageBrokerHealthCheck());
        environment.jersey().register(new SearchResource());
        environment.jersey().register(new XQueryResource());
        environment.jersey().register(new StatusResource());
    }
}
