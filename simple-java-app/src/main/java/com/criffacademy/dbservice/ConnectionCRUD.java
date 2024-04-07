package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class ConnectionCRUD {

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

    public int getLatestConnectionId() throws SQLException, IOException {
        String SQL = "SELECT MAX(id_connection) FROM connection";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Impossibile recuperare l'ultimo ID di connessione");
            }
        }
    }
    
    // Metodo CREATE aggiornato
    public void addConnection(String publicIp, int sourcePort, boolean isConnected) throws SQLException, IOException {
        String SQL = "INSERT INTO connection(public_ip, source_port, isConnected) VALUES(CAST(? AS INET),?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, publicIp);
            pstmt.setInt(2, sourcePort);
            pstmt.setBoolean(3, isConnected);
            pstmt.executeUpdate();
            System.out.println("Connessione aggiunta con successo.");
        }
    }
    

    // READ
    public void getConnection(int connectionId) throws SQLException, IOException {
        String SQL = "SELECT id_connection, id_session, public_ip, source_port, isConnected FROM connection WHERE id_connection = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, connectionId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Connection ID: " + rs.getInt("id_connection") + ", Session ID: " + rs.getInt("id_session") +
                ", Public IP: " + rs.getString("public_ip") + ", Source Port: " + rs.getInt("source_port") +
                ", Is Connected: " + rs.getBoolean("isConnected"));
            } else {
                System.out.println("Connessione non trovata.");
            }
        }
    }

    // UPDATE - Ad esempio, aggiornamento dello stato della connessione
    public void updateConnectionStatus(int connectionId, boolean isConnected) throws SQLException, IOException {
        String SQL = "UPDATE connection SET isConnected = ? WHERE id_connection = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setBoolean(1, isConnected);
            pstmt.setInt(2, connectionId);
            pstmt.executeUpdate();
        }
    }
    

    // DELETE
    public void deleteConnection(int connectionId) throws SQLException, IOException {
        String SQL = "DELETE FROM connection WHERE id_connection = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, connectionId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Connessione eliminata con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }
}