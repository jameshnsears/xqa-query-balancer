package xqa.integration.fixtures;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xqa.XqaQueryBalancerApplication;
import xqa.XqaQueryBalancerConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;

public class DatabaseFixture {
    @ClassRule
    public static final DropwizardAppRule<XqaQueryBalancerConfiguration> application = new DropwizardAppRule<>(
            XqaQueryBalancerApplication.class,
            ResourceHelpers.resourceFilePath("xqa-query-balancer.yml"));
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFixture.class);

    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("database").getPath();
    }

    protected void setupStorage() throws Exception {
        Class.forName(application.getConfiguration().getDataSourceFactory().getDriverClass());
        Connection connection = DriverManager.getConnection(
                application.getConfiguration().getDataSourceFactory().getUrl(),
                application.getConfiguration().getDataSourceFactory().getUser(),
                application.getConfiguration().getDataSourceFactory().getPassword());
        truncate(connection);
        populate(connection);
        connection.close();
    }

    private void populate(Connection connection) throws IOException {
        try (Stream<Path> filePathStream = Files.walk(Paths.get(getResource()))) {
            filePathStream.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    try {
                        insertFileContentsIntoDatabase(connection, filePath);
                    } catch (Exception exception) {
                        logger.error(exception.getMessage());
                    }
                }
            });
        }
    }

    private void truncate(Connection connection) {
        try {
            executeSql(connection, "truncate events;");
        } catch (SQLException exception) {
            logger.error(exception.getMessage());
        }
    }

    private void executeSql(Connection connection, String sql) throws SQLException {
        Statement statement;
        statement = connection.createStatement();
        statement.execute(sql);
        statement.close();
    }

    private void insertFileContentsIntoDatabase(Connection connection, Path filePath)
            throws Exception {
        try (Stream<String> stream = Files.lines(filePath)) {
            stream.forEach(line -> {
                try {
                    executeSql(connection, line);
                } catch (SQLException exception) {
                    logger.error(exception.getMessage());
                }
            });
        }
    }
}
