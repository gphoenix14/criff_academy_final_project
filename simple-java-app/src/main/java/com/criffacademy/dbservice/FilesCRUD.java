package com.criffacademy.dbservice;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import org.springframework.stereotype.Component;

@Component
public class FilesCRUD {

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
    public void addFile(int senderId, String filePath) throws SQLException, IOException {
        String SQL = "INSERT INTO files(sender_id, file_path) VALUES(?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, senderId);
            pstmt.setString(2, filePath);
            pstmt.executeUpdate();
            System.out.println("File aggiunto con successo.");
        }
    }

    // READ
    public void getFile(int fileId) throws SQLException, IOException {
        String SQL = "SELECT id_file, sender_id, file_path FROM files WHERE id_file = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, fileId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("File ID: " + rs.getInt("id_file") + ", Sender ID: " + rs.getInt("sender_id") +
                ", File Path: " + rs.getString("file_path"));
            } else {
                System.out.println("File non trovato.");
            }
        }
    }

    // UPDATE
    public void updateFile(int fileId, String filePath) throws SQLException, IOException {
        String SQL = "UPDATE files SET file_path = ? WHERE id_file = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, filePath);
            pstmt.setInt(2, fileId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Percorso del file aggiornato con successo.");
            } else {
                System.out.println("Aggiornamento non riuscito.");
            }
        }
    }

    // DELETE
    public void deleteFile(int fileId) throws SQLException, IOException {
        String SQL = "DELETE FROM files WHERE id_file = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, fileId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("File eliminato con successo.");
            } else {
                System.out.println("Eliminazione non riuscita.");
            }
        }
    }
}
