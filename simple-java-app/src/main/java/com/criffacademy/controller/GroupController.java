package com.criffacademy.controller;

import com.criffacademy.cryptoservice.TokenUtils;
import com.criffacademy.dbservice.GroupsCRUD;
import com.criffacademy.dbservice.UsersGroupsCRUD;
import com.criffacademy.dbservice.UsersCRUD;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class GroupController {
    private static GroupsCRUD groupsCrud = new GroupsCRUD();
    private static UsersGroupsCRUD usersGroupsCrud = new UsersGroupsCRUD();
    private static UsersCRUD usersCrud = new UsersCRUD();

    // Metodo per la creazione di un nuovo gruppo
    public void createGroup(String username, String jwt, String groupName, String groupPassword, String enigmaPSK, String aesPSK, int cesarShift, int defaultCrypto) throws NoSuchAlgorithmException, SQLException, IOException {
        // Verifica la validità del refresh token e ottiene un nuovo JWT

        if (!TokenUtils.verifyJWT(jwt)) {
            throw new SecurityException("Token JWT non valido o scaduto.");
        }

        boolean exists = groupsCrud.groupExists(groupName);


        if (exists) {
            throw new IllegalArgumentException("Il gruppo esiste già.");
        }
        
        // Aggiunge il gruppo al database
        String hashedGroupPassword = hashPassword(groupPassword); // Utilizza l'hash SHA-512 per la password del gruppo
        groupsCrud.addGroup(groupName, hashedGroupPassword, enigmaPSK, aesPSK, cesarShift, defaultCrypto);
        
        // Recupera l'ID dell'ultimo gruppo inserito (assumiamo che esista un metodo per farlo)
        int groupId = groupsCrud.getLastGroupId();
        
        // Recupera l'ID dell'utente basato sul nome utente
        int userId = UserController.getUserIdByUsername(username);
        
        // Aggiunge l'utente al gruppo come proprietario
        usersGroupsCrud.addUserToGroup(userId, groupId, true);
    }

    // Metodo per aggiornare i dettagli di un gruppo
    public void updateGroupDetails(String username, String jwt, int groupId, String newGroupName, String newEnigmaPSK, String newAesPSK, int newCesarShift, int newDefaultCrypto) throws NoSuchAlgorithmException, SQLException, IOException {
        // Verifica la validità del refresh token e ottiene un nuovo JWT
        if (!TokenUtils.verifyJWT(jwt)) {
            throw new SecurityException("Token JWT non valido o scaduto.");
            }
        
        // Verifica se l'utente è il proprietario del gruppo
        if (!usersGroupsCrud.isUserOwnerOfGroup(UserController.getUserIdByUsername(username), groupId)) {
            throw new SecurityException("L'utente non ha i permessi per aggiornare i dettagli del gruppo.");
        }
        
        // Aggiorna i dettagli del gruppo
        groupsCrud.updateGroup(groupId, newGroupName, newEnigmaPSK, newAesPSK, newCesarShift, newDefaultCrypto);
    }

    public void deleteGroup(int groupId, String username) throws Exception {
        // Recupera l'ID dell'utente basato sul nome utente
        int userId = UserController.getUserIdByUsername(username);
        
        // Verifica se l'utente è il proprietario del gruppo
        if (!usersGroupsCrud.isUserOwnerOfGroup(userId, groupId)) {
            throw new SecurityException("Non hai i permessi per eliminare questo gruppo.");
        }
        
        // Procede con l'eliminazione del gruppo
        GroupsCRUD.removeGroupById(groupId);
        System.out.println("Gruppo eliminato con successo.");
    }
    

    // Metodo ausiliario per hashare le password con SHA-512
    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public boolean verifyGroupPassword(String groupName, String password, String jwt) throws Exception {
        // Recupera la password hashata del gruppo dal database

        if (!TokenUtils.verifyJWT(jwt)) {
            throw new SecurityException("Token JWT non valido o scaduto.");
        }

        String storedHash = groupsCrud.getGroupPasswordHash(groupName);
        if (storedHash == null) {
            throw new Exception("Gruppo non trovato");
        }
        
        // Hasha la password fornita per il confronto
        String passwordHash = hashPassword(password);
        
        // Confronta i due hash
        return storedHash.equals(passwordHash);
    }

    public int getGroupIDFromGroupName(String groupName, String jwt) throws SQLException, IOException {
        // Chiama il metodo definito in GroupsCRUD per ottenere l'ID del gruppo
        if (!TokenUtils.verifyJWT(jwt)) {
            throw new SecurityException("Token JWT non valido o scaduto.");
        }
        return GroupsCRUD.getGroupIDFromGroupName(groupName);
    }

    public static void addUserToGroup(String username, String groupName, boolean isOwner, String jwt) throws SQLException, IOException, NoSuchAlgorithmException {
        // Prima, ottieni l'ID dell'utente dato il suo username

        if (!TokenUtils.verifyJWT(jwt)) {
            throw new SecurityException("Token JWT non valido o scaduto.");
        }

        int userId = UserController.getUserIdByUsername(username);

        // Poi, ottieni l'ID del gruppo dato il suo nome
        int groupId = GroupsCRUD.getGroupIDFromGroupName(groupName);

        // Ora, usa il metodo di UsersGroupsCRUD per aggiungere l'utente al gruppo
        usersGroupsCrud.addUserToGroup(userId, groupId, isOwner);

        System.out.println("Utente " + username + " aggiunto al gruppo " + groupName + " con successo.");
    }

}
