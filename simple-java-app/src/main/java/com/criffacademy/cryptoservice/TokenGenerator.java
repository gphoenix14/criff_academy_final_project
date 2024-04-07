package com.criffacademy.cryptoservice;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class TokenGenerator {

    private static String secretKey = "defaultSecret";
    private static int refreshExpiry = 3600000 * 24 * 7; // 7 giorni
    private static int jwtExpiry = 3600000; // 1 ora

    static {
        loadConfiguration("/com/criffacademy/app.properties");
    }

    private static void loadConfiguration(String configFilePath) {
        Properties prop = new Properties();
        try (InputStream inputStream = TokenGenerator.class.getResourceAsStream(configFilePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Configurazione per JWT non trovata: " + configFilePath);
            }
            prop.load(inputStream);
            secretKey = prop.getProperty("jwt.psk", secretKey);
            refreshExpiry = Integer.parseInt(prop.getProperty("jwt.refreshExpiry", String.valueOf(refreshExpiry)));
            jwtExpiry = Integer.parseInt(prop.getProperty("jwt.expiry", String.valueOf(jwtExpiry)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metodo per generare un nuovo JWT se il refresh token è valido
    public static String generateJWTFromRefreshToken(String refreshToken, String username) {
        // Qui dovresti inserire la logica per verificare la validità del refresh token
        boolean isValidRefreshToken = true; // Placeholder per la validità del refresh token

        if (isValidRefreshToken) {
            // Genera un nuovo JWT se il refresh token è valido
            return generateJWT(username);
        } else {
            throw new IllegalArgumentException("Refresh token non valido.");
        }
    }

    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("deprecation")
    public static String generateJWT(String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + jwtExpiry;
        Date exp = new Date(expMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }
}
