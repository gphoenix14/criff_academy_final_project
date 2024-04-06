package com.criffacademy.controller;
import com.criffacademy.dbservice.*;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserController {

    private UsersCRUD usersCrud = new UsersCRUD();
    private SessionsCRUD sessionsCrud = new SessionsCRUD();

    public String user_login(String username, String password) {
        // Qui dovrebbe essere implementato un metodo in UsersCRUD per verificare le credenziali
        // Poiché non è fornito, ipotizziamo che esista un metodo per questo scopo
        try {
            if (usersCrud.verifyUser(username, password)) {
                // Se le credenziali sono corrette, creiamo una sessione
                String sessionToken = "TOKEN_GENERATO"; // Qui si dovrebbe generare un token univoco per la sessione
                // Assumiamo che l'ID utente sia recuperato durante la verifica
                int userId = usersCrud.getUserIdByUsername(username);
                sessionsCrud.addSession(userId, sessionToken, new Timestamp(System.currentTimeMillis()), null, 0); // ID connessione fittizio, in un caso reale sarebbe diverso
                return sessionToken;
            } else {
                return "InvalidCredentials";
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return "Errore durante il login";
        }
    }

    public String user_signup(String username, String password) {
        try {
            if (usersCrud.checkUserExists(username)) {
                return "username already taken";
            } else {
                // Conversione della password in SHA-512
                String hashedPassword = hashPassword(password);
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
            usersCrud.updateUserStealthMode(username, true);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void reset_stealthMode(String username) {
        try {
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
