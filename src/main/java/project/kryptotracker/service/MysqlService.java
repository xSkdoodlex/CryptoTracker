package project.kryptotracker.service;

import project.kryptotracker.config.DatabaseConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class MysqlService {

    @Autowired private DatabaseConfiguration databaseConfiguration;

    public int teste() {
        try {
            Connection con = databaseConfiguration.getConnection();
            String sqlStatement;
            Statement stmt;
            sqlStatement = "select * from Adresse";
            stmt = con.createStatement();
            ResultSet resultSet = stmt.executeQuery(sqlStatement);
            int ct=0;
            while (resultSet.next()) ct++;
            con.close();
            return ct;
        } catch (Exception e) {
            return -1;
        }
    }
}
