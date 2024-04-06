package com.criffacademy.controller;
import com.criffacademy.dbservice.*;
import com.criffacademy.cryptoservice.TokenGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

public class UserController {
    

    private UsersCRUD usersCrud = new UsersCRUD();
    private SessionsCRUD sessionsCrud = new SessionsCRUD();
    private ConnectionCRUD connectionCrud = new ConnectionCRUD();


    public String user_login(String username, String password) {
        try {
            // Hash della password
            String hashedPassword = hashPassword(password);
            
            // Verifica delle credenziali e gestione connessione
            if (usersCrud.verifyUser(username, hashedPassword)) {
                String sessionToken = TokenGenerator.generateToken(username);
                int userId = usersCrud.getUserIdByUsername(username);
                
                // Aggiungi dettagli connessione (adattare con valori reali)
                String publicIp = "127.0.0.1"; // Esempio, ottenere il vero IP se necessario
                int sourcePort = 12345; // Esempio, ottenere la vera porta se necessario
                connectionCrud.addConnection(publicIp, sourcePort, true);
                // Recupera l'ID di connessione inserito (implementare il metodo getLatestConnectionId o simile)
                int connectionId = connectionCrud.getLatestConnectionId();
                
                sessionsCrud.addSession(userId, sessionToken, new Timestamp(System.currentTimeMillis()), connectionId);
                return sessionToken;
            } else {
                return "InvalidCredentials";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Errore durante il login";
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
