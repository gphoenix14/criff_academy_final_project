package com.criffacademy.cryptoservice;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class TokenGenerator {

    private static final String SECRET_KEY_PROPERTY = "jwt.psk";
    private static final String EXPIRY_PROPERTY = "jwt.expiry";
    private static String secretKey = "defaultSecret"; // Un valore di fallback
    private static int expiry = 3600000; // Un valore di fallback (es. 1 ora in millisecondi)

    static {
        loadConfiguration("/com/criffacademy/app.properties"); // Assicurati che il percorso corrisponda alla tua struttura di progetto
    }

    private static void loadConfiguration(String configFilePath) {
        Properties prop = new Properties();
        try (InputStream inputStream = TokenGenerator.class.getResourceAsStream(configFilePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Configurazione per JWT non trovata: " + configFilePath);
            }
            prop.load(inputStream);
            secretKey = prop.getProperty(SECRET_KEY_PROPERTY, secretKey);
            expiry = Integer.parseInt(prop.getProperty(EXPIRY_PROPERTY, String.valueOf(expiry)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateToken(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + expiry;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
