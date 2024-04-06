package com.criffacademy.cryptoservice;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class TokenGenerator {

    private static final String SECRET_KEY_PROPERTY = "jwt.psk";
    private static final String REFRESH_EXPIRY_PROPERTY = "jwt.refreshExpiry";
    private static final String JWT_EXPIRY_PROPERTY = "jwt.expiry";
    private static String secretKey = "defaultSecret"; // Un valore di fallback
    private static int refreshExpiry = 3600000 * 24 * 7; // Un valore di fallback per refresh token (es. 7 giorni in millisecondi)
    private static int jwtExpiry = 3600000; // Un valore di fallback per JWT (es. 1 ora in millisecondi)

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
            refreshExpiry = Integer.parseInt(prop.getProperty(REFRESH_EXPIRY_PROPERTY, String.valueOf(refreshExpiry)));
            jwtExpiry = Integer.parseInt(prop.getProperty(JWT_EXPIRY_PROPERTY, String.valueOf(jwtExpiry)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String generateRefreshToken(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + refreshExpiry;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public static String generateJWT(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        long expMillis = nowMillis + jwtExpiry; // Usa la scadenza specifica per JWT
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
