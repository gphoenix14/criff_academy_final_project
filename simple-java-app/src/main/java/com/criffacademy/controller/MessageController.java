package com.criffacademy.controller;

import com.criffacademy.cryptoservice.TokenUtils;
import com.criffacademy.dbservice.MessagesCRUD;
import com.criffacademy.dbservice.UsersCRUD;
import com.criffacademy.dbservice.GroupsCRUD;

import java.io.IOException; // Corretto il package per IOException
import java.sql.SQLException;
import java.sql.Timestamp; // Aggiunto per il supporto di Timestamp
import java.util.List; // Aggiunto per il supporto di Liste

import org.springframework.stereotype.Component;

@Component

public class MessageController {

    private MessagesCRUD messagesCRUD;
    private TokenUtils tokenUtils;
    private GroupsCRUD groupsCRUD; // Nomi delle variabili dovrebbero seguire le convenzioni Java
    private UsersCRUD usersCrud;

    public MessageController() {
        this.messagesCRUD = new MessagesCRUD();
        this.groupsCRUD = new GroupsCRUD();
        this.tokenUtils = new TokenUtils();
        this.usersCrud = new UsersCRUD();
    }

    public void sendMessage(String username, String jwtToken, boolean isUnicast, boolean isMulticast,
                            boolean isBroadcast, String groupDstName, String userDstUsername,
                            boolean hasAttachment, Integer attachmentId, String msgText, Timestamp now) throws IOException {
        if (TokenUtils.verifyJWT(jwtToken)) {
            try {
                int senderId = usersCrud.getUserIdByUsername(username);
                
                Integer groupDstId = null, userDstId = null;
                
                if (groupDstName != null && !groupDstName.isEmpty()) {
                    groupDstId = GroupsCRUD.getGroupIDFromGroupName(groupDstName); // Assicurati che questo metodo restituisca il corretto ID
                }
                
                if (userDstUsername != null && !userDstUsername.isEmpty()) {
                    userDstId = usersCrud.getUserIdByUsername(userDstUsername); // Assicurati che questo metodo restituisca il corretto ID
                }
                
                // Ora passiamo anche il Timestamp quando chiamiamo addMessage
                messagesCRUD.addMessage(senderId, isUnicast, isMulticast, isBroadcast, groupDstId, userDstId, hasAttachment, attachmentId, msgText, now);
                
                System.out.println("Messaggio inviato con successo.");
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Errore durante l'invio del messaggio: " + e.getMessage());
            }
        } else {
            System.out.println("Token JWT non valido.");
        }
    }

    public void readMessages(String username, String groupName, String jwtToken, String datetime) {
        // Verifica la validità del JWT
        if (TokenUtils.verifyJWT(jwtToken)) {
            try {
                Timestamp sinceDateTime = Timestamp.valueOf(datetime);
                List<String> messages = messagesCRUD.getMessagesAfterDateTime(username, groupName, sinceDateTime);

                if (messages.isEmpty()) {
                    System.out.println("Nessun messaggio trovato dopo la data specificata.");
                } else {
                    messages.forEach(System.out::println);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Errore durante la lettura dei messaggi: " + e.getMessage());
            }
        } else {
            System.out.println("Token JWT non valido.");
        }
    }
}
