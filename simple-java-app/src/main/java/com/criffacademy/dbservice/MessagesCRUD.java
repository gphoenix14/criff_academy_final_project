package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MessagesCRUD {

    private Connection connect() throws SQLException, IOException {
        Properties props = new Properties();
        InputStream is = getClass().getClassLoader().getResourceAsStream("com/criffacademy/app.properties");
        if (is == null) {
            throw new SQLException("Impossibile trovare il file app.properties");
        }
        props.load(is);

        String url = props.getProperty("db.url") + "?ssl=" + props.getProperty("db.ssl") + "&sslmode=" + props.getProperty("db.sslmode");
        Properties connProps = new Properties();
        connProps.setProperty("user", props.getProperty("db.user"));
        connProps.setProperty("password", props.getProperty("db.password"));

        if (Boolean.parseBoolean(props.getProperty("db.ssl"))) {
            connProps.setProperty("ssl", "true");
            connProps.setProperty("sslmode", props.getProperty("db.sslmode"));
        }

        return DriverManager.getConnection(url, connProps);
    }

    // CREATE
    public void addMessage(int senderId, boolean isUnicast, boolean isMulticast, boolean isBroadcast, 
    Integer groupDstId, Integer userDstId, boolean hasAttachment, 
    Integer attachmentId, String msgText, Timestamp msgTimestamp) throws SQLException, IOException {
        String SQL = "INSERT INTO messages(sender_id, isUnicast, isMulticast, isBroadcast, " +
        "group_dst_id, user_dst_id, hasAttachment, attachment_id, msg_text, msg_timestamp) " +
        "VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = connect();
        PreparedStatement pstmt = conn.prepareStatement(SQL)) {
        pstmt.setInt(1, senderId);
        pstmt.setBoolean(2, isUnicast);
        pstmt.setBoolean(3, isMulticast);
        pstmt.setBoolean(4, isBroadcast);
        if (groupDstId != null) {
        pstmt.setInt(5, groupDstId);
        } else {
        pstmt.setNull(5, Types.INTEGER);
        }
        if (userDstId != null) {
        pstmt.setInt(6, userDstId);
        } else {
        pstmt.setNull(6, Types.INTEGER);
        }
        pstmt.setBoolean(7, hasAttachment);
        if (hasAttachment && attachmentId != null) {
        pstmt.setInt(8, attachmentId);
        } else {
        pstmt.setNull(8, Types.INTEGER); // Qui si gestisce il caso di messaggi senza allegati
        }
        pstmt.setString(9, msgText);
        pstmt.setTimestamp(10, msgTimestamp);
        pstmt.executeUpdate();
        System.out.println("Messaggio aggiunto con successo.");
        }
}




    // READ
    public void getMessage(int messageId) throws SQLException, IOException {
        String SQL = "SELECT id_message, sender_id, isUnicast, isMulticast, isBroadcast, " +
                     "group_dst_id, user_dst_id, hasAttachment, attachment_id, msg_text, msg_timestamp " +
                     "FROM messages WHERE id_message = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();
    
            if (rs.next()) {
                System.out.println("Message ID: " + rs.getInt("id_message") +
                                   ", Sender ID: " + rs.getInt("sender_id") +
                                   ", Is Unicast: " + rs.getBoolean("isUnicast") +
                                   ", Is Multicast: " + rs.getBoolean("isMulticast") +
                                   ", Is Broadcast: " + rs.getBoolean("isBroadcast") +
                                   ", Group Destination ID: " + rs.getObject("group_dst_id") + // Utilizzo getObject per gestire null
                                   ", User Destination ID: " + rs.getObject("user_dst_id") + // Utilizzo getObject per gestire null
                                   ", Has Attachment: " + rs.getBoolean("hasAttachment") +
                                   ", Attachment ID: " + rs.getObject("attachment_id") + // Utilizzo getObject per gestire null
                                   ", Message Text: " + rs.getString("msg_text") +
                                   ", Timestamp: " + rs.getTimestamp("msg_timestamp").toString());
            } else {
                System.out.println("Messaggio non trovato.");
            }
        }
    }
    



    // UPDATE
    public void updateMessage(int messageId, String msgText) throws SQLException, IOException {
        String SQL = "UPDATE messages SET msg_text = ? WHERE id_message = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, msgText);
            pstmt.setInt(2, messageId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Messaggio aggiornato con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE
    public void deleteMessage(int messageId) throws SQLException, IOException {
        String SQL = "DELETE FROM messages WHERE id_message = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, messageId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Messaggio eliminato con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }

    public List<String> getMessagesAfterDateTime(String username, String groupName, Timestamp sinceDateTime) throws SQLException, IOException {
        List<String> messages = new ArrayList<>();
        String SQL = "SELECT m.msg_text, m.msg_timestamp FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "JOIN groups g ON m.group_dst_id = g.group_id " +
                     "WHERE u.username = ? AND g.group_name = ? AND m.msg_timestamp > ?";
    
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, groupName);
            pstmt.setTimestamp(3, sinceDateTime);
    
            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                String msgText = rs.getString("msg_text") + " at " + rs.getTimestamp("msg_timestamp");
                messages.add(msgText);
            }
        }
        return messages;
    }
    
}
