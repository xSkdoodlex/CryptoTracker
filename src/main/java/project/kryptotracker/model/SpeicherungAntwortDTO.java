package project.kryptotracker.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SpeicherungAntwortDTO {

    private String username;
    private String password;

    private int anzahlCoins;

    // Wichtig: Map von Symbol (z. B. BTC) auf Liste von CoinData-Einträgen
    private Map<String, List<CoinData>> bestand;

}
