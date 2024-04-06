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

    public String user_login(String username, String password, String publicIp, int sourcePort) throws NoSuchAlgorithmException, SQLException, IOException {
        // Verifica credenziali
        String hashedPassword = hashPassword(password);
        if (!usersCrud.verifyUser(username, hashedPassword)) {
            return "InvalidCredentials";
        }
        
        // Credenziali corrette: genera JWT e refresh token
        int userId = usersCrud.getUserIdByUsername(username);
        String jwt = TokenGenerator.generateRefreshToken(username); // Genera JWT
        String refreshToken = TokenGenerator.generateRefreshToken(username); // Genera refresh token
        
        // Aggiungi dettagli connessione
        connectionCrud.addConnection(publicIp, sourcePort, true);
        int connectionId = connectionCrud.getLatestConnectionId();
        
        // Aggiungi sessione (refresh token) al database, includendo l'ID di connessione
        sessionsCrud.addSession(userId, refreshToken, new Timestamp(System.currentTimeMillis()), connectionId);

        // Restituisce JWT al client
        return jwt;
    }
    
    public void user_logout(String refreshToken) throws SQLException, IOException {
        // Verifica e cancella la sessione usando il refresh token
        sessionsCrud.deleteSessionByRefreshToken(refreshToken);
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
