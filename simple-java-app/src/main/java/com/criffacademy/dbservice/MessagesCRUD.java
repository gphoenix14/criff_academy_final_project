package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
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
    public void addMessage(int senderId, boolean isUnicast, boolean isMulticast, boolean isBroadcast, int groupDstId, boolean hasAttachment, int attachmentId, String msgText) throws SQLException, IOException {
        String SQL = "INSERT INTO messages(sender_id, isUnicast, isMulticast, isBroadcast, group_dst_id, hasAttachment, attachment_id, msg_text) VALUES(?,?,?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, senderId);
            pstmt.setBoolean(2, isUnicast);
            pstmt.setBoolean(3, isMulticast);
            pstmt.setBoolean(4, isBroadcast);
            pstmt.setInt(5, groupDstId);
            pstmt.setBoolean(6, hasAttachment);
            pstmt.setInt(7, attachmentId);
            pstmt.setString(8, msgText);
            pstmt.executeUpdate();
            System.out.println("Messaggio aggiunto con successo.");
        }
    }

    // READ
    public void getMessage(int messageId) throws SQLException, IOException {
        String SQL = "SELECT id_message, sender_id, isUnicast, isMulticast, isBroadcast, group_dst_id, hasAttachment, attachment_id, msg_text FROM messages WHERE id_message = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Message ID: " + rs.getInt("id_message") + ", Sender ID: " + rs.getInt("sender_id") +
                ", Is Unicast: " + rs.getBoolean("isUnicast") + ", Is Multicast: " + rs.getBoolean("isMulticast") +
                ", Is Broadcast: " + rs.getBoolean("isBroadcast") + ", Group Destination ID: " + rs.getInt("group_dst_id") +
                ", Has Attachment: " + rs.getBoolean("hasAttachment") + ", Attachment ID: " + rs.getInt("attachment_id") +
                ", Message Text: " + rs.getString("msg_text"));
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
}
