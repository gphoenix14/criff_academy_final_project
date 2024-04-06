package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class GroupsCRUD {

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
    public void addGroup(String groupName, String enigmaPSK, String aesPSK, int cesarShift, int defaultCrypto) throws SQLException, IOException {
        String SQL = "INSERT INTO groups(group_name, enigmaPSK, aesPSK, cesarshift, defaultCrypto) VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, groupName);
            pstmt.setString(2, enigmaPSK);
            pstmt.setString(3, aesPSK);
            pstmt.setInt(4, cesarShift);
            pstmt.setInt(5, defaultCrypto);
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
}
