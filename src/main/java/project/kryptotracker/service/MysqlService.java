package project.kryptotracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.kryptotracker.config.DatabaseConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@Service
public class MysqlService {

    private static final String API_URL = "https://api.bitpanda.com/v1/asset-wallets";

    @Autowired
    private DatabaseConfiguration databaseConfiguration;

    public boolean checkPasswort(String benutzername, String passwort) {
        String sql = "SELECT * FROM Stammdaten WHERE Benutzername = ? AND Passwort = ?";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, benutzername);
            stmt.setString(2, passwort);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true wenn Eintrag gefunden
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String evaluateAPIKEY(String benutzername, String passwort) {
        String sql = "SELECT API FROM Stammdaten WHERE Benutzername = ? AND Passwort = ?";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, benutzername);
            stmt.setString(2, passwort);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("API");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Kein API-Key gefunden"; // kein API-Key gefunden
    }

    public String getBitpandaData(String apiKey) {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Api-Key", apiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public void saveCoinBalances(String username, Map<String, Double> newCoinBalances) {
        try (Connection con = databaseConfiguration.getConnection()) {

            // 1. Lade aktuelle Bestände des Benutzers
            String selectSql = "SELECT CoinName, Menge FROM CoinBestände WHERE Benutzername = ?";
            Map<String, Double> existingBalances = new HashMap<>();

            try (PreparedStatement selectStmt = con.prepareStatement(selectSql)) {
                selectStmt.setString(1, username);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    while (rs.next()) {
                        existingBalances.put(rs.getString("CoinName"), rs.getDouble("Menge"));
                    }
                }
            }

            // 2. Update oder Insert falls nötig
            for (Map.Entry<String, Double> entry : newCoinBalances.entrySet()) {
                String coin = entry.getKey();
                double newAmount = entry.getValue();

                if (existingBalances.containsKey(coin)) {
                    double oldAmount = existingBalances.get(coin);
                    if (Double.compare(oldAmount, newAmount) != 0) {
                        // UPDATE
                        String updateSql = "UPDATE CoinBestände SET Menge = ? WHERE Benutzername = ? AND CoinName = ?";
                        try (PreparedStatement updateStmt = con.prepareStatement(updateSql)) {
                            updateStmt.setDouble(1, newAmount);
                            updateStmt.setString(2, username);
                            updateStmt.setString(3, coin);
                            updateStmt.executeUpdate();
                        }
                    }
                } else {
                    // INSERT
                    String insertSql = "INSERT INTO CoinBestände (Benutzername, CoinName, Menge) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, coin);
                        insertStmt.setDouble(3, newAmount);
                        insertStmt.executeUpdate();
                    }
                }
            }

            // 3. Lösche Coins, die nicht mehr im neuen Portfolio sind
            for (String existingCoin : existingBalances.keySet()) {
                if (!newCoinBalances.containsKey(existingCoin)) {
                    String deleteSql = "DELETE FROM CoinBestände WHERE Benutzername = ? AND CoinName = ?";
                    try (PreparedStatement deleteStmt = con.prepareStatement(deleteSql)) {
                        deleteStmt.setString(1, username);
                        deleteStmt.setString(2, existingCoin);
                        deleteStmt.executeUpdate();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
