package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class SessionsCRUD {

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

    // Metodo CREATE aggiornato
    public void addSession(int userId, String refreshToken, Timestamp startSessionDate, int idConnection) throws SQLException, IOException {
        String SQL = "INSERT INTO sessions(user_id, refresh_token, start_session_date, id_connection) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, refreshToken);
            pstmt.setTimestamp(3, startSessionDate);
            pstmt.setInt(4, idConnection); // Aggiunge l'ID di connessione come parametro
            pstmt.executeUpdate();
            System.out.println("Sessione aggiunta con successo.");
        }
    }
    
    
    
    
    public void deleteSessionByRefreshToken(String refreshToken) throws SQLException, IOException {
        String SQL = "DELETE FROM sessions WHERE refresh_token = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, refreshToken);
            pstmt.executeUpdate();
        }
    }

    // READ
    public void getSession(int sessionId) throws SQLException, IOException {
        String SQL = "SELECT id_session, user_id, token, start_session_date, end_session_date, id_connection FROM sessions WHERE id_session = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Session ID: " + rs.getInt("id_session") + ", User ID: " + rs.getInt("user_id") + 
                ", Token: " + rs.getString("token") + ", Start Session Date: " + rs.getTimestamp("start_session_date") +
                ", End Session Date: " + rs.getTimestamp("end_session_date"));
            } else {
                System.out.println("Sessione non trovata.");
            }
        }
    }

    // UPDATE - generalmente le sessioni non sono aggiornate, ma per completezza ecco un esempio
    public void updateSessionEnd(int sessionId, Timestamp newEndSessionDate) throws SQLException, IOException {
        String SQL = "UPDATE sessions SET end_session_date = ? WHERE id_session = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setTimestamp(1, newEndSessionDate);
            pstmt.setInt(2, sessionId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Data fine sessione aggiornata con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE
    public void deleteSession(int sessionId) throws SQLException, IOException {
        String SQL = "DELETE FROM sessions WHERE id_session = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, sessionId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Sessione eliminata con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }

    public int[] findSessionAndConnectionIdByToken(String token) throws SQLException, IOException {
        String SQL = "SELECT id_session, id_connection FROM sessions WHERE token = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, token);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new int[]{rs.getInt("id_session"), rs.getInt("id_connection")};
                }
            }
        }
        return new int[]{-1, -1}; // Restituisce -1 se non trova la sessione o la connessione
    }
    
}
