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

    // Metodo CREATE aggiornato per includere la scadenza del refresh token
    public void addSession(int userId, String refreshToken, Timestamp startSessionDate, Timestamp expiresAt, int idConnection) throws SQLException, IOException {
        String SQL = "INSERT INTO sessions(user_id, refresh_token, start_session_date, expires_at, id_connection) VALUES(?,?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, refreshToken);
            pstmt.setTimestamp(3, startSessionDate);
            pstmt.setTimestamp(4, expiresAt); // Aggiunge il timestamp di scadenza
            pstmt.setInt(5, idConnection);
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
            System.out.println("Sessione eliminata con successo tramite refresh token.");
        }
    }

    // Metodo per ottenere i dettagli di una sessione - nessuna modifica necessaria
    public void getSession(int sessionId) throws SQLException, IOException {
        String SQL = "SELECT id_session, user_id, start_session_date, expires_at, id_connection FROM sessions WHERE id_session = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, sessionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Session ID: " + rs.getInt("id_session") + ", User ID: " + rs.getInt("user_id") +
                ", Start Session Date: " + rs.getTimestamp("start_session_date") +
                ", Expires At: " + rs.getTimestamp("expires_at") +
                ", Connection ID: " + rs.getInt("id_connection"));
            } else {
                System.out.println("Sessione non trovata.");
            }
        }
    }

    // Metodo per aggiornare la data di fine di una sessione - nessuna modifica necessaria
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

    // Metodo per eliminare una sessione - nessuna modifica necessaria
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

    // Metodo per trovare una sessione e l'ID della connessione tramite token - questo metodo potrebbe richiedere aggiustamenti se si intende usare il refresh token al posto del token
    public int[] findSessionAndConnectionIdByToken(String refreshToken) throws SQLException, IOException {
        String SQL = "SELECT id_session, id_connection FROM sessions WHERE refresh_token = ?";
        int[] result = {-1, -1};
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, refreshToken);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result[0] = rs.getInt("id_session");
                result[1] = rs.getInt("id_connection");
                System.out.println("Trovato: Session ID = " + result[0] + ", Connection ID = " + result[1]);
            } else {
                System.out.println("Nessuna sessione o connessione trovata per il refresh token fornito: " + refreshToken);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    
    
}
