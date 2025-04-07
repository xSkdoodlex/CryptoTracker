package project.kryptotracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;

@Configuration
public class DatabaseConfiguration {

    @Value("${MYSQL_SERVER:localhost}")
    private String mysqlServer;
    @Value("${MYSQL_PORT:3306}")
    private int mysqlPort;
    @Value("${MYSQL_USER:root}")
    private String mysqlUser;
    @Value("${MYSQL_PASSWORD:}")
    private String mysqlPassword;
    @Value("${MYSQL_DATABASE:kryptotracker}")
    private String mysqlDatabase;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);

    @Bean
    @Scope("prototype")
    public Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + mysqlServer + ":" + mysqlPort + "/" + mysqlDatabase
                + "?useSSL=false&serverTimezone=UTC";
        logger.info("Versuche Verbindung zu: {}", url);

        try {
            Connection con = DriverManager.getConnection(url, mysqlUser, mysqlPassword);
            logger.info("Verbindung erfolgreich!");
            return con;
        } catch (SQLException e) {
            logger.error("Verbindung fehlgeschlagen: {}", e.getMessage());
            throw e;  // Weiterwerfen, damit Services reagieren können
        }
    }
}