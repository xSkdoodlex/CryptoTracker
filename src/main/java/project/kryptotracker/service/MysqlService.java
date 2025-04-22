package project.kryptotracker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import project.kryptotracker.config.DatabaseConfiguration;
import project.kryptotracker.model.CoinData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MysqlService {

    private static final String API_URL = "https://api.bitpanda.com/v1/asset-wallets";
    private static final String CRYPTO_NEWS_API_URL = "https://cryptopanic.com/api/v1/posts/";

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

    public void saveCoinBalances(String username, Map<String, List<CoinData>> newCoinBalances, String currentTime) {
        try (Connection con = databaseConfiguration.getConnection()) {
            con.setAutoCommit(false);

            // 1. Alte Einträge löschen
            String deleteSql = "DELETE FROM CoinBestände WHERE Benutzername = ?";
            try (PreparedStatement deleteStmt = con.prepareStatement(deleteSql)) {
                deleteStmt.setString(1, username);
                deleteStmt.executeUpdate();
            }

            // 2. Neue Einträge einfügen
            String insertSql = "INSERT INTO CoinBestände (Benutzername, CoinName, Menge, Preis, Wert, Datum) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                for (Map.Entry<String, List<CoinData>> entry : newCoinBalances.entrySet()) {
                    String coinName = entry.getKey();
                    for (CoinData data : entry.getValue()) {
                        insertStmt.setString(1, username);
                        insertStmt.setString(2, coinName);
                        insertStmt.setDouble(3, data.getAmount());
                        insertStmt.setDouble(4, data.getPrice());
                        insertStmt.setDouble(5, data.getValue());
                        insertStmt.setString(6, currentTime);
                        insertStmt.addBatch();
                    }
                }
                insertStmt.executeBatch();
            }

            con.commit();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Speichern", e);
        }
    }
    private boolean coinExists(Connection con, String username, String coinName) throws SQLException {
        String sql = "SELECT 1 FROM CoinBestände WHERE Benutzername = ? AND CoinName = ?";
        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, coinName);
            return stmt.executeQuery().next();
        }
    }

    public boolean benutzerExistiert(String username) {
        String sql = "SELECT 1 FROM Stammdaten WHERE Benutzername = ?";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerBenutzer(String username, String password, String apiKey) {
        String sql = "INSERT INTO Stammdaten (Benutzername, Passwort, API) VALUES (?, ?, ?)";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, apiKey);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, List<CoinData>> getSavedCoinBalances(String username) {
        Map<String, List<CoinData>> result = new HashMap<>();
        String sql = "SELECT CoinName, Menge, Preis, Wert FROM CoinBestände WHERE Benutzername = ?";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String coinName = rs.getString("CoinName");
                    double menge = rs.getDouble("Menge");
                    double preis = rs.getDouble("Preis");
                    double wert = rs.getDouble("Wert");

                    CoinData data = new CoinData(menge, preis, wert);
                    // Logging hinzufügen, um zu überprüfen, welche Coins zurückgegeben werden
                    System.out.println("CoinName: " + coinName + ", Menge: " + menge + ", Preis: " + preis + ", Wert: " + wert);

                    result.computeIfAbsent(coinName, k -> new java.util.ArrayList<>()).add(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public List<Map<String, Object>> fetchNewsFromCryptoPanic(String symbols) {
        String apiKey = "221018bbedc160ad492b3f833fe8bc4967a5c480";
        String url = "https://cryptopanic.com/api/v1/posts/?auth_token=" + apiKey + "&currencies=" + symbols;

        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        return (List<Map<String, Object>>) response.get("results");
    }
    public String getLastSavedTime(String username) {
        String sql = "SELECT MAX(Datum) as lastSaved FROM CoinBestände WHERE Benutzername = ?";
        try (Connection con = databaseConfiguration.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString("lastSaved") : null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }















}
