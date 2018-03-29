package xqa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import xqa.resources.messagebroker.MessageBrokerConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class XqaQueryBalancerConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();
    private MessageBrokerConfiguration messageBrokerConfiguration = new MessageBrokerConfiguration();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("messageBroker")
    public MessageBrokerConfiguration getMessageBrokerConfiguration() {
        return messageBrokerConfiguration;
    }

    @JsonProperty("messageBroker")
    public void setMessageBrokerConfiguration(MessageBrokerConfiguration factory) {
        this.messageBrokerConfiguration = factory;
    }
}
