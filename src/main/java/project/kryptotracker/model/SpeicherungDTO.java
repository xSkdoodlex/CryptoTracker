package project.kryptotracker.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class SpeicherungDTO {
    private String username;
    private String password;
    private int anzahlCoins;
    private Map<String, Double> bestand;

}
