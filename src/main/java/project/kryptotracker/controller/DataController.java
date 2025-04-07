package project.kryptotracker.controller;

import project.kryptotracker.model.PasswortAbfrageDTO;
import project.kryptotracker.model.PasswortAntwortDTO;

import project.kryptotracker.model.SpeicherungAntwortDTO;
import project.kryptotracker.service.MysqlService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping
public class DataController {

    private String API_KEY = null;

    @Autowired
    private MysqlService mysqlService;

    @PostMapping("/api/login")
    public ResponseEntity<PasswortAntwortDTO> login(@RequestBody PasswortAbfrageDTO dto) {
        // Logik zur Prüfung der Benutzeranmeldung

        boolean isValidUser = mysqlService.checkPasswort(dto.getUsername(), dto.getPassword());
        if (isValidUser) {
            API_KEY = mysqlService.evaluateAPIKEY(dto.getUsername(), dto.getPassword());
            return ResponseEntity.ok(new PasswortAntwortDTO(true,"Erfolgreich eingeloggt"));
        } else {
            return ResponseEntity.ok(new PasswortAntwortDTO(false,"Loggin Fehlgeschlagen"));
        }
    }

    @GetMapping("/WalletAbfrage")
    public ResponseEntity<String> BitpandaData() {
        String antwort = mysqlService.getBitpandaData(API_KEY);
        return ResponseEntity.ok(antwort);

    }

    @PostMapping("/api/speicherung")
    public ResponseEntity<String> saveData(@RequestBody SpeicherungAntwortDTO dto2) {
        // Die Coin-Bestände als Map vorbereiten
        Map<String, Double> coinBalances = dto2.getBestand(); // erwartet eine Map von CoinName zu Menge

        // Speichern der Bestände in der Datenbank
        try {
            mysqlService.saveCoinBalances(dto2.getUsername(), coinBalances);
            return ResponseEntity.ok("Bestände erfolgreich gespeichert.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Fehler beim Speichern der Bestände.");
        }
    }

}
