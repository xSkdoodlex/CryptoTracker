package project.kryptotracker.controller;

import org.springframework.http.HttpStatus;
import project.kryptotracker.model.*;
import project.kryptotracker.service.MysqlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class DataController {

    private String API_KEY = null;

    @Autowired
    private MysqlService mysqlService;

    @PostMapping("/api/login")
    public ResponseEntity<PasswortAntwortDTO> login(@RequestBody PasswortAbfrageDTO dto) {
        boolean isValidUser = mysqlService.checkPasswort(dto.getUsername(), dto.getPassword());
        if (isValidUser) {
            API_KEY = mysqlService.evaluateAPIKEY(dto.getUsername(), dto.getPassword());
            return ResponseEntity.ok(new PasswortAntwortDTO(true, "Erfolgreich eingeloggt"));
        } else {
            return ResponseEntity.ok(new PasswortAntwortDTO(false, "Login fehlgeschlagen"));
        }
    }

    @GetMapping("/WalletAbfrage")
    public ResponseEntity<String> bitpandaData() {
        if (API_KEY == null) {
            return ResponseEntity.status(401).body("Nicht eingeloggt.");
        }

        String antwort = mysqlService.getBitpandaData(API_KEY);
        return ResponseEntity.ok(antwort);
    }

    @PostMapping("/api/speicherung")
    public ResponseEntity<String> saveData(@RequestBody SpeicherungAntwortDTO dto) {
        try {
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
            mysqlService.saveCoinBalances(dto.getUsername(), dto.getBestand(), currentTime);
            return ResponseEntity.ok("Daten gespeichert um " + currentTime);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fehler: " + e.getMessage());
        }
    }




    @PostMapping("/api/register")
    public ResponseEntity<String> register(@RequestBody RegistrierungDTO dto) {
        boolean exists = mysqlService.benutzerExistiert(dto.getUsername());
        if (exists) {
            return ResponseEntity.badRequest().body("Benutzername ist bereits vergeben.");
        }

        boolean success = mysqlService.registerBenutzer(dto.getUsername(), dto.getPassword(), dto.getApiKey());
        if (success) {
            return ResponseEntity.ok("Benutzer erfolgreich registriert.");
        } else {
            return ResponseEntity.status(500).body("Fehler beim Registrieren.");
        }
    }

    @GetMapping("/api/offline-bestaende")
    public ResponseEntity<Map<String, List<CoinData>>> getOfflineBestand(@RequestParam String username) {

        try {
            Map<String, List<CoinData>> bestaende = mysqlService.getSavedCoinBalances(username);
            return ResponseEntity.ok(bestaende);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/api/news")
    public ResponseEntity<?> getCryptoNews(@RequestParam("symbols") String symbols) {
        try {
            List<Map<String, Object>> news = mysqlService.fetchNewsFromCryptoPanic(symbols.toLowerCase());
            return ResponseEntity.ok(news);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler beim Abrufen der News: " + e.getMessage());
        }
    }

    @GetMapping("/api/lastSaved")
    public ResponseEntity<String> getLastSavedTime(@RequestParam String username) {
        try {
            String lastSaved = mysqlService.getLastSavedTime(username);
            return ResponseEntity.ok(lastSaved != null ? lastSaved : "Nie gespeichert");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Fehler beim Abrufen");
        }
    }














}
