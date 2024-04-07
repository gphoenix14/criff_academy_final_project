package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class GroupsCRUD {

    private static Connection connection = null;

    private static void initializeConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            InputStream is = GroupsCRUD.class.getClassLoader().getResourceAsStream("com/criffacademy/app.properties");
            if (is == null) {
                throw new IOException("Impossibile trovare il file app.properties");
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

            connection = DriverManager.getConnection(url, connProps);
        }
    }

    private static Connection connect() throws SQLException, IOException {
        initializeConnection();
        return connection;
    }

    // CREATE
    public void addGroup(String groupName, String groupPassword, String enigmaPSK, String aesPSK, int cesarShift, int defaultCrypto) throws SQLException, IOException {
        String SQL = "INSERT INTO groups(group_name, group_password, enigmaPSK, aesPSK, cesarshift, defaultCrypto) VALUES(?,?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, groupPassword); // Aggiunta della password del gruppo
            pstmt.setString(3, enigmaPSK);
            pstmt.setString(4, aesPSK);
            pstmt.setInt(5, cesarShift);
            pstmt.setInt(6, defaultCrypto);
            pstmt.executeUpdate();
            System.out.println("Gruppo aggiunto con successo.");
        }
    }
    

    // READ
    public void getGroup(int groupId) throws SQLException, IOException {
        String SQL = "SELECT group_id, group_name, enigmaPSK, aesPSK, cesarshift, defaultCrypto FROM groups WHERE group_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Group ID: " + rs.getInt("group_id") + ", Group Name: " + rs.getString("group_name") +
                        ", Enigma PSK: " + rs.getString("enigmaPSK") + ", AES PSK: " + rs.getString("aesPSK") +
                        ", Cesar Shift: " + rs.getInt("cesarshift") + ", Default Crypto: " + rs.getInt("defaultCrypto"));
            }
        }
    }

    // UPDATE
    public void updateGroup(int groupId, String groupName, String enigmaPSK, String aesPSK, int cesarShift, int defaultCrypto) throws SQLException, IOException {
        String SQL = "UPDATE groups SET group_name = ?, enigmaPSK = ?, aesPSK = ?, cesarshift = ?, defaultCrypto = ? WHERE group_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, enigmaPSK);
            pstmt.setString(3, aesPSK);
            pstmt.setInt(4, cesarShift);
            pstmt.setInt(5, defaultCrypto);
            pstmt.setInt(6, groupId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Gruppo aggiornato con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE
    public void deleteGroup(int groupId) throws SQLException, IOException {
        String SQL = "DELETE FROM groups WHERE group_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, groupId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Gruppo eliminato con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }

    // Metodo aggiunto: Ottiene l'ID dell'ultimo gruppo inserito
    public int getLastGroupId() throws SQLException, IOException {
        String SQL = "SELECT MAX(group_id) AS last_id FROM groups";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("last_id");
            } else {
                throw new SQLException("Impossibile recuperare l'ultimo ID del gruppo.");
            }
        }
    }

    public static void removeGroupById(int groupId) throws SQLException, IOException {
        // Questo metodo ora utilizza la connessione inizializzata staticamente
        Connection conn = connect();
        String SQL = "DELETE FROM groups WHERE group_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, groupId);
            int affectedRows = pstmt.executeUpdate();
    
            if (affectedRows > 0) {
                System.out.println("Gruppo eliminato con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }

    public String getGroupPasswordHash(String groupName) throws SQLException, IOException {
        String SQL = "SELECT group_password FROM groups WHERE group_name = ?";
        try (Connection conn = connect(); // Utilizza il metodo connect della tua classe
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
    
            if (rs.next()) {
                return rs.getString("group_password");
            } else {
                return null; // Gruppo non trovato
            }
        }
    }

    public static Integer getGroupIDFromGroupName(String groupName) throws SQLException, IOException {
        String SQL = "SELECT group_id FROM groups WHERE group_name = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("group_id");
            } else {
                return null; // O gestisci l'assenza dell'ID in un altro modo
            }
        }
    }

    public boolean groupExists(String groupName) throws SQLException, IOException {
        String SQL = "SELECT COUNT(*) FROM groups WHERE group_name = ?";
        try (Connection conn = connect(); // Utilizza il metodo connect già definito nella classe
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Se il conteggio è maggiore di 0, significa che il gruppo esiste
                return rs.getInt(1) > 0;
            } else {
                // Se non ci sono risultati dalla query, il gruppo non esiste
                return false;
            }
        }
    }
    

    
    
}
