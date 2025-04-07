package project.kryptotracker.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpeicherungAntwortDTO {
    private String username;
    private String password;
    private Map<String, Double> bestand;
}
