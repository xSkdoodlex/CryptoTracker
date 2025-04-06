package project.kryptotracker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class DatabaseConfiguration {

    @Value("${MYSQL_SERVER:localhost}")
    private String mysqlServer;
    @Value("${MYSQL_PORT:3306}")
    private int mysqlPort;
    @Value("${MYSQL_USER}")
    private String mysqlUser;
    @Value("${MYSQL_PASSWORD}")
    private String mysqlPassword;
    @Value("${MYSQL_DATABASE}")
    private String mysqlDatabase;

    Logger logger = Logger.getLogger(DatabaseConfiguration.class.getName());

    public Connection getConnection() {
        String url = "jdbc:mysql://"+mysqlServer + ":" + mysqlPort + "/" + mysqlDatabase;
        try {
            Connection con = DriverManager.getConnection(url,mysqlUser,mysqlPassword);
            logger.info("Database connection established!");
            return con;
        } catch (SQLException e) {
            logger.warning("Database connection failed! "+e.getMessage());
            return null;
        }
    }


}
