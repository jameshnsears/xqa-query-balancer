package xqa.integration;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Stream;


public class SetupDatabase {
    private static final Logger logger = LoggerFactory.getLogger(SetupDatabase.class);

    private String getResource() {
        return Thread.currentThread().getContextClassLoader().getResource("populate-database").getPath();
    }

    @Test
    public void setupDatabase() throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection connection = DriverManager.getConnection("jdbc:postgresql://0.0.0.0:5432/xqa", "xqa", "xqa");
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

    private void insertFileContentsIntoDatabase(Connection connection, Path filePath) throws Exception {
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
