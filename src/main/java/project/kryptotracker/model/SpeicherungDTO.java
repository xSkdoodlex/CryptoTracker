package project.kryptotracker.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeicherungDTO {
    private String username;
    private String password;
    private Map<String, CoinData> bestand; // CoinData enthält Menge, Preis und Wert
}
