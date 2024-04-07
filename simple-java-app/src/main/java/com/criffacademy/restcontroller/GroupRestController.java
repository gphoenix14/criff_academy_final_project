package com.criffacademy.restcontroller;

import com.criffacademy.controller.UserController;
import com.criffacademy.cryptoservice.TokenUtils;
import com.criffacademy.dbservice.GroupsCRUD;
import com.criffacademy.dbservice.UsersGroupsCRUD;
import com.criffacademy.dbservice.UsersCRUD;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

@RestController
@RequestMapping("/api/group")
public class GroupRestController {

    @Autowired
    private GroupsCRUD groupsCrud;
    @Autowired
    private UsersGroupsCRUD usersGroupsCrud;
    @Autowired
    private UsersCRUD usersCrud;

    @PostMapping("/create")
    @Autowired
    public ResponseEntity<?> createGroup(@RequestParam String username, @RequestHeader("Authorization") String jwt,
                                         @RequestParam String groupName, @RequestParam String groupPassword,
                                         @RequestParam String enigmaPSK, @RequestParam String aesPSK,
                                         @RequestParam int cesarShift, @RequestParam int defaultCrypto)
            throws NoSuchAlgorithmException, SQLException, IOException {

        if (!TokenUtils.verifyJWT(jwt.substring(7))) {
            return ResponseEntity.status(403).body("Token JWT non valido o scaduto.");
        }

        if (groupsCrud.groupExists(groupName)) {
            return ResponseEntity.badRequest().body("Il gruppo esiste già.");
        }

        String hashedGroupPassword = hashPassword(groupPassword);
        groupsCrud.addGroup(groupName, hashedGroupPassword, enigmaPSK, aesPSK, cesarShift, defaultCrypto);

        int groupId = groupsCrud.getLastGroupId();
        int userId = UserController.getUserIdByUsername(username);

        usersGroupsCrud.addUserToGroup(userId, groupId, true);

        return ResponseEntity.ok("Gruppo creato con successo.");
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateGroupDetails(@RequestParam String username, @RequestHeader("Authorization") String jwt,
                                                @RequestParam int groupId, @RequestParam String newGroupName,
                                                @RequestParam String newEnigmaPSK, @RequestParam String newAesPSK,
                                                @RequestParam int newCesarShift, @RequestParam int newDefaultCrypto)
            throws NoSuchAlgorithmException, SQLException, IOException {

        if (!TokenUtils.verifyJWT(jwt.substring(7))) {
            return ResponseEntity.status(403).body("Token JWT non valido o scaduto.");
        }

        if (!usersGroupsCrud.isUserOwnerOfGroup(UserController.getUserIdByUsername(username), groupId)) {
            return ResponseEntity.status(403).body("L'utente non ha i permessi per aggiornare i dettagli del gruppo.");
        }

        groupsCrud.updateGroup(groupId, newGroupName, newEnigmaPSK, newAesPSK, newCesarShift, newDefaultCrypto);

        return ResponseEntity.ok("Dettagli del gruppo aggiornati con successo.");
    }

    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable int groupId, @RequestHeader("Authorization") String jwt,
                                         @RequestParam String username) throws Exception {

        if (!TokenUtils.verifyJWT(jwt.substring(7))) {
            return ResponseEntity.status(403).body("Token JWT non valido o scaduto.");
        }

        int userId = UserController.getUserIdByUsername(username);

        if (!usersGroupsCrud.isUserOwnerOfGroup(userId, groupId)) {
            return ResponseEntity.status(403).body("Non hai i permessi per eliminare questo gruppo.");
        }

        GroupsCRUD.removeGroupById(groupId);

        return ResponseEntity.ok("Gruppo eliminato con successo.");
    }

    @PostMapping("/verifyPassword")
public ResponseEntity<?> verifyGroupPassword(@RequestParam String groupName, @RequestParam String password,
                                             @RequestHeader("Authorization") String jwt) {
    try {
        if (!TokenUtils.verifyJWT(jwt.substring(7))) {
            return ResponseEntity.status(403).body("Token JWT non valido o scaduto.");
        }
        
        String storedHash = groupsCrud.getGroupPasswordHash(groupName);
        if (storedHash == null) {
            return ResponseEntity.notFound().build();
        }
        
        String passwordHash = hashPassword(password);
        
        boolean isPasswordCorrect = storedHash.equals(passwordHash);
        if (isPasswordCorrect) {
            return ResponseEntity.ok("La password del gruppo è corretta.");
        } else {
            return ResponseEntity.status(403).body("Password del gruppo non corretta.");
        }
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Errore nella verifica della password del gruppo: " + e.getMessage());
    }
}

@GetMapping("/getId/{groupName}")
public ResponseEntity<?> getGroupIDFromGroupName(@PathVariable String groupName, @RequestHeader("Authorization") String jwt) {
    try {
        if (!TokenUtils.verifyJWT(jwt.substring(7))) {
            return ResponseEntity.status(403).body("Token JWT non valido o scaduto.");
        }
        
        int groupId = GroupsCRUD.getGroupIDFromGroupName(groupName);
        if (groupId == -1) { // Assumendo che -1 sia il valore di errore
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(groupId);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Errore nel recupero dell'ID del gruppo: " + e.getMessage());
    }
}



    private String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // I metodi verifyGroupPassword e getGroupIDFromGroupName possono essere implementati seguendo lo stesso schema
    // mostrato qui sopra, adeguandoli come necessario per le loro specifiche operazioni.
}
