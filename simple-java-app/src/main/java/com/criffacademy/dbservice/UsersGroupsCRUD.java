package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class UsersGroupsCRUD {

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
    public void addUserToGroup(int userId, int groupId, boolean isOwner) throws SQLException, IOException {
        String SQL = "INSERT INTO users_groups(user_id, group_id, isowner) VALUES(?,?,?)";
        try (Connection conn = connect(); // Assicurati che questo sia il metodo di connessione corretto
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            pstmt.setBoolean(3, isOwner); // Questo sarà false per gli utenti che si uniscono, true solo per il creatore
            pstmt.executeUpdate();
            System.out.println("Utente aggiunto al gruppo con successo.");
        }
    }

    // READ (per un singolo gruppo, potrebbe essere esteso)
    public void getUsersByGroup(int groupId) throws SQLException, IOException {
        String SQL = "SELECT users_groups_id, user_id, group_id, isOwner FROM users_groups WHERE group_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, groupId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Users_Groups ID: " + rs.getInt("users_groups_id") + ", User ID: " + rs.getInt("user_id") + 
                ", Group ID: " + rs.getInt("group_id") + ", Is Owner: " + rs.getBoolean("isOwner"));
            }
        }
    }

    // UPDATE - cambia il flag 'isOwner' per un utente in un gruppo
    public void updateUserGroup(int usersGroupsId, boolean isOwner) throws SQLException, IOException {
        String SQL = "UPDATE users_groups SET isOwner = ? WHERE users_groups_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setBoolean(1, isOwner);
            pstmt.setInt(2, usersGroupsId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Informazioni utente-gruppo aggiornate con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE - rimuove un utente da un gruppo
    public void removeUserFromGroup(int usersGroupsId) throws SQLException, IOException {
        String SQL = "DELETE FROM users_groups WHERE users_groups_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, usersGroupsId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Utente rimosso dal gruppo con successo.");
            } else {
                System.out.println("Rimozione non riuscita.");
            }
        }
    }

    public boolean isUserOwnerOfGroup(int userId, int groupId) throws SQLException, IOException {
        String SQL = "SELECT COUNT(*) FROM users_groups WHERE user_id = ? AND group_id = ? AND isOwner = TRUE";
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
             
            pstmt.setInt(1, userId);
            pstmt.setInt(2, groupId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            
            return false;
        }
    }
}
