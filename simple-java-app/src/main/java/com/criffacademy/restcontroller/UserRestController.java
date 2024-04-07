package com.criffacademy.restcontroller;

import com.criffacademy.dbservice.UsersCRUD;
import com.criffacademy.general.LoginResponse;
import com.criffacademy.dbservice.SessionsCRUD;
import com.criffacademy.dbservice.ConnectionCRUD;
import com.criffacademy.cryptoservice.TokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    @Autowired
    private UsersCRUD usersCrud;
    @Autowired
    private SessionsCRUD sessionsCrud;
    @Autowired
    ConnectionCRUD connectionCrud;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> user_login(@RequestParam String username, @RequestParam String password, 
                                                    @RequestParam String publicIp, @RequestParam int sourcePort) {
        try {
            String hashedPassword = hashPassword(password);
            if (!usersCrud.verifyUser(username, hashedPassword)) {
                return ResponseEntity.badRequest().body(null); // Considera l'uso di una risposta più specifica
            }

            int userId = usersCrud.getUserIdByUsername(username);
            String jwt = TokenUtils.generateJWT(username);
            String refreshToken = TokenUtils.generateRefreshToken(username);
            long refreshTokenExpiry = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7); // 7 giorni
            Timestamp expiresAt = new Timestamp(refreshTokenExpiry);

            connectionCrud.addConnection(publicIp, sourcePort, true);
            int connectionId = connectionCrud.getLatestConnectionId();

            sessionsCrud.addSession(userId, refreshToken, new Timestamp(System.currentTimeMillis()), expiresAt, connectionId);

            return ResponseEntity.ok(new LoginResponse(jwt, refreshToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> user_logout(@RequestParam String refreshToken) {
        try {
            int[] sessionAndConnectionIds = sessionsCrud.findSessionAndConnectionIdByToken(refreshToken);
            int sessionId = sessionAndConnectionIds[0];
            int connectionId = sessionAndConnectionIds[1];

            if (sessionId != -1 && connectionId != -1) {
                sessionsCrud.deleteSessionByRefreshToken(refreshToken);
                connectionCrud.updateConnectionStatus(connectionId, false);
                return ResponseEntity.ok("Logout effettuato con successo. Stato della connessione aggiornato.");
            } else {
                return ResponseEntity.badRequest().body("Errore: Sessione o Connessione non trovata per il refresh token fornito.");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante il logout.");
        }
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> userExists(@RequestParam String username) {
        try {
            boolean exists = usersCrud.checkUserExists(username);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> user_signup(@RequestParam String username, @RequestParam String password) {
        try {
            if (usersCrud.checkUserExists(username)) {
                return ResponseEntity.badRequest().body("username already taken");
            } else {
                String hashedPassword = hashPassword(password);
                usersCrud.addUser(username, hashedPassword, false);
                return ResponseEntity.ok("Signed up successfully!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore durante la registrazione");
        }
    }

    @PostMapping("/stealthMode")
    public ResponseEntity<Void> set_stealthMode(@RequestParam String username, @RequestParam boolean enable) {
        try {
            usersCrud.updateUserStealthMode(username, enable);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/refreshJWT")
public ResponseEntity<String> refreshJWT(@RequestParam String refreshToken, @RequestParam String username) {
    try {
        // Verifica la validità del refresh token
        boolean isValidRefreshToken = sessionsCrud.checkRefreshTokenValid(refreshToken, username);
        if (isValidRefreshToken) {
            String newJwt = TokenUtils.generateJWT(username);
            return ResponseEntity.ok(newJwt);
        } else {
            // Token non valido o scaduto
            return ResponseEntity.status(403).body("Refresh token non valido o scaduto.");
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Errore durante il rinnovo del JWT: " + e.getMessage());
    }
}


    // I metodi refreshJWT, getUserIdByUsername e hashPassword sono definiti come nell'originale, adattandoli come necessario.
    // Ricordati di aggiungere le necessarie annotazioni @Autowired per le dipendenze e di gestire le eccezioni in modo appropriato.

    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = md.digest(password.getBytes());
        return bytesToHex(hashedBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
