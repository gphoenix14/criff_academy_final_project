package com.criffacademy.controller;

import com.criffacademy.dbservice.UsersCRUD;
import com.criffacademy.general.LoginResponse;
import com.criffacademy.dbservice.SessionsCRUD;
import com.criffacademy.dbservice.ConnectionCRUD;
import com.criffacademy.cryptoservice.TokenUtils;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.security.MessageDigest;

public class UserController {
    private static UsersCRUD usersCrud = new UsersCRUD();
    private SessionsCRUD sessionsCrud = new SessionsCRUD();
    private ConnectionCRUD connectionCrud = new ConnectionCRUD();

    public LoginResponse user_login(String username, String password, String publicIp, int sourcePort) throws NoSuchAlgorithmException, SQLException, IOException {
        String hashedPassword = hashPassword(password);
        if (!usersCrud.verifyUser(username, hashedPassword)) {
            return null; // Potresti lanciare un'eccezione personalizzata qui.
        }
        
        int userId = usersCrud.getUserIdByUsername(username);
        String jwt = TokenUtils.generateJWT(username);
        String refreshToken = TokenUtils.generateRefreshToken(username);
        long refreshTokenExpiry = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 7); // 7 giorni
        Timestamp expiresAt = new Timestamp(refreshTokenExpiry);
        
        connectionCrud.addConnection(publicIp, sourcePort, true);
        int connectionId = connectionCrud.getLatestConnectionId();
        
        sessionsCrud.addSession(userId, refreshToken, new Timestamp(System.currentTimeMillis()), expiresAt, connectionId);
        
        return new LoginResponse(jwt, refreshToken);
    }
    
    public void user_logout(String refreshToken) throws SQLException, IOException {
        int[] sessionAndConnectionIds = sessionsCrud.findSessionAndConnectionIdByToken(refreshToken);
        int sessionId = sessionAndConnectionIds[0];
        int connectionId = sessionAndConnectionIds[1];
    
        if (sessionId != -1 && connectionId != -1) {
            sessionsCrud.deleteSessionByRefreshToken(refreshToken);
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
            if (usersCrud.checkUserExists(username)) {
                return "username already taken";
            } else {
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

    // Metodo aggiunto per rinfrescare il JWT utilizzando un refresh token
    public String refreshJWT(String refreshToken, String username) throws NoSuchAlgorithmException, SQLException, IOException {
        // Assumiamo che sessionsCrud.checkRefreshTokenValid esista e verifichi la validità del refreshToken per l'username fornito
        boolean isValidRefreshToken = sessionsCrud.checkRefreshTokenValid(refreshToken, username);
        if (isValidRefreshToken) {
            return TokenUtils.generateJWT(username);
        } else {
            // Considera la possibilità di lanciare un'eccezione specifica se il token non è valido o è scaduto
            throw new SecurityException("Refresh token non valido o scaduto.");
        }
    }

    public static int getUserIdByUsername(String username) throws SQLException, IOException {
        return usersCrud.getUserIdByUsername(username);
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