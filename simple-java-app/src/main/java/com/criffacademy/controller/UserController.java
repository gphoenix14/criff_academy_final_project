package com.criffacademy.controller;
import com.criffacademy.dbservice.*;
import com.criffacademy.cryptoservice.TokenGenerator;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.security.MessageDigest;


public class UserController {
    private UsersCRUD usersCrud = new UsersCRUD();
    private SessionsCRUD sessionsCrud = new SessionsCRUD();
    private ConnectionCRUD connectionCrud = new ConnectionCRUD();

    public LoginResponse user_login(String username, String password, String publicIp, int sourcePort) throws NoSuchAlgorithmException, SQLException, IOException {
        // Verifica credenziali
        String hashedPassword = hashPassword(password);
        if (!usersCrud.verifyUser(username, hashedPassword)) {
            return null; // Invece di restituire null, potresti considerare di lanciare un'eccezione o restituire un oggetto LoginResponse con JWT e refresh token null/empty.
        }
        
        int userId = usersCrud.getUserIdByUsername(username);
        
        // Genera JWT
        String jwt = TokenGenerator.generateJWT(username); // Assumi che esista un metodo simile per JWT
        
        // Genera refresh token con scadenza
        String refreshToken = TokenGenerator.generateRefreshToken(username);
        // Calcola la scadenza per il refresh token (ad esempio, 7 giorni)
        long refreshTokenExpiry = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7); // 7 giorni
        Timestamp expiresAt = new Timestamp(refreshTokenExpiry);
        
        // Aggiungi dettagli connessione
        connectionCrud.addConnection(publicIp, sourcePort, true);
        int connectionId = connectionCrud.getLatestConnectionId();
        
        // Aggiungi sessione (refresh token) al database, includendo l'ID di connessione e il timestamp di scadenza
        sessionsCrud.addSession(userId, refreshToken, new Timestamp(System.currentTimeMillis()), expiresAt, connectionId);
        
        // Restituisce un oggetto LoginResponse contenente sia il JWT che il refresh token
        return new LoginResponse(jwt, refreshToken);
    }
    
    
    public void user_logout(String refreshToken) throws SQLException, IOException {
        // Recupera l'ID della sessione e l'ID della connessione associati al refresh token fornito
        int[] sessionAndConnectionIds = sessionsCrud.findSessionAndConnectionIdByToken(refreshToken);
        int sessionId = sessionAndConnectionIds[0];
        int connectionId = sessionAndConnectionIds[1];
    
        if (sessionId != -1 && connectionId != -1) {
            // Cancellazione della sessione tramite refresh token
            sessionsCrud.deleteSessionByRefreshToken(refreshToken);
    
            // Aggiornamento dello stato della connessione a 'non connesso'
            connectionCrud.updateConnectionStatus(connectionId, false);
            System.out.println("Logout effettuato con successo. Stato della connessione aggiornato.");
        } else {
            System.out.println("Errore: Sessione o Connessione non trovata per il refresh token fornito.");
        }
    }
    
    
    public boolean userExists(String username) {
        try {
            return usersCrud.checkUserExists(username);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String user_signup(String username, String password) {
        try {
            // Verifica se l'username esiste già
            if (usersCrud.checkUserExists(username)) {
                return "username already taken";
            } else {
                // Conversione della password in SHA-512
                String hashedPassword = hashPassword(password);
                // Aggiunge l'utente nel database
                usersCrud.addUser(username, hashedPassword, false);
                return "Signed up successfully!";
            }
        } catch (NoSuchAlgorithmException | SQLException | IOException e) {
            e.printStackTrace();
            return "Errore durante la registrazione";
        }
    }

    public void set_stealthMode(String username) {
        try {
            // Aggiorna lo stato stealth dell'utente a true
            usersCrud.updateUserStealthMode(username, true);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void reset_stealthMode(String username) {
        try {
            // Aggiorna lo stato stealth dell'utente a false
            usersCrud.updateUserStealthMode(username, false);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

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
