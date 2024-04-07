package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class UsersCRUD {

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
    public void addUser(String username, String pwd, boolean isStealth) throws SQLException, IOException {
        String SQL = "INSERT INTO users(username, pwd, isStealth) VALUES(?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pwd);
            pstmt.setBoolean(3, isStealth);
            pstmt.executeUpdate();
            System.out.println("Utente aggiunto con successo.");
        }
    }

    // READ
    public void getUser(int userId) throws SQLException, IOException {
        String SQL = "SELECT id, username, pwd, isStealth FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Username: " + rs.getString("username") + ", Password: " + rs.getString("pwd") + ", IsStealth: " + rs.getBoolean("isStealth"));
            }
        }
    }

    // UPDATE
    public void updateUser(int userId, String username, String pwd, boolean isStealth) throws SQLException, IOException {
        String SQL = "UPDATE users SET username = ?, pwd = ?, isStealth = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, pwd);
            pstmt.setBoolean(3, isStealth);
            pstmt.setInt(4, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Utente aggiornato con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE
    public void deleteUser(int userId) throws SQLException, IOException {
        String SQL = "DELETE FROM users WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, userId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Utente eliminato con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }
    public boolean verifyUser(String username, String hashedPassword) throws SQLException, IOException {
        String SQL = "SELECT COUNT(*) FROM users WHERE username = ? AND pwd = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    
        // Metodo aggiuntivo per controllare se un utente esiste già
        public boolean checkUserExists(String username) throws SQLException, IOException {
            String SQL = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        }
    
        // Metodo aggiuntivo per ottenere l'ID dell'utente in base al nome utente
        public int getUserIdByUsername(String username) throws SQLException, IOException {
            String SQL = "SELECT id FROM users WHERE username = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                } else {
                    throw new SQLException("Utente non trovato");
                }
            }
        }
    
        // Metodo aggiuntivo per aggiornare lo stato stealth dell'utente
        public void updateUserStealthMode(String username, boolean isStealth) throws SQLException, IOException {
            String SQL = "UPDATE users SET isStealth = ? WHERE username = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {
                pstmt.setBoolean(1, isStealth);
                pstmt.setString(2, username);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Aggiornamento dello stato stealth non riuscito.");
                }
            }
        }
    }

