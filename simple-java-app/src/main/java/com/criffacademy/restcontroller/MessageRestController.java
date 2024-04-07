package com.criffacademy.restcontroller;

import com.criffacademy.cryptoservice.TokenUtils;
import com.criffacademy.dbservice.MessagesCRUD;
import com.criffacademy.dbservice.UsersCRUD;
import com.criffacademy.dbservice.GroupsCRUD;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageRestController {

    private final MessagesCRUD messagesCRUD;
    private final TokenUtils tokenUtils;
    private final GroupsCRUD groupsCRUD;
    private final UsersCRUD usersCrud;

    public MessageRestController(MessagesCRUD messagesCRUD, GroupsCRUD groupsCRUD, TokenUtils tokenUtils, UsersCRUD usersCrud) {
        this.messagesCRUD = messagesCRUD;
        this.groupsCRUD = groupsCRUD;
        this.tokenUtils = tokenUtils;
        this.usersCrud = usersCrud;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestParam String username, @RequestHeader("Authorization") String jwtToken,
                              @RequestParam boolean isUnicast, @RequestParam boolean isMulticast,
                              @RequestParam boolean isBroadcast, @RequestParam(required = false) String groupDstName,
                              @RequestParam(required = false) String userDstUsername, @RequestParam boolean hasAttachment,
                              @RequestParam(required = false) Integer attachmentId, @RequestParam String msgText,
                              @RequestParam Timestamp now) {
        try {
            if (TokenUtils.verifyJWT(jwtToken)) {
                int senderId = usersCrud.getUserIdByUsername(username);

                Integer groupDstId = null, userDstId = null;

                if (groupDstName != null && !groupDstName.isEmpty()) {
                    groupDstId = GroupsCRUD.getGroupIDFromGroupName(groupDstName);
                }

                if (userDstUsername != null && !userDstUsername.isEmpty()) {
                    userDstId = usersCrud.getUserIdByUsername(userDstUsername);
                }

                messagesCRUD.addMessage(senderId, isUnicast, isMulticast, isBroadcast, groupDstId, userDstId, hasAttachment, attachmentId, msgText, now);

                return "Messaggio inviato con successo.";
            } else {
                return "Token JWT non valido.";
            }
        } catch (SQLException | IOException e) {
            return "Errore durante l'invio del messaggio: " + e.getMessage();
        }
    }

    @GetMapping("/read")
    public List<String> readMessages(@RequestParam String username, @RequestParam String groupName,
                                     @RequestHeader("Authorization") String jwtToken, @RequestParam String datetime) {
        try {
            if (TokenUtils.verifyJWT(jwtToken)) {
                Timestamp sinceDateTime = Timestamp.valueOf(datetime);
                return messagesCRUD.getMessagesAfterDateTime(username, groupName, sinceDateTime);
            } else {
                throw new SecurityException("Token JWT non valido.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Errore durante la lettura dei messaggi: " + e.getMessage());
        }
    }
}
