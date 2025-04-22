package project.kryptotracker.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class RegistrierungDTO {
    private String username;
    private String password;
    private String apiKey;

    // Getter und Setter
}
