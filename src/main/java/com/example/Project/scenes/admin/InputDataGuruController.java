package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InputDataGuruController {

    @FXML
    private Label nipLabel;
    @FXML
    private TextField namaGuruField;
    @FXML
    private PasswordField passwordField;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        nipLabel.setText("(Akan dibuat otomatis)");
    }

    private String generateNewNip(Connection conn) throws SQLException {
        String lastNipSql = "SELECT nip FROM guru WHERE nip LIKE 'G%' ORDER BY CAST(SUBSTRING(nip, 2) AS INTEGER) DESC LIMIT 1";
        PreparedStatement pstmt = conn.prepareStatement(lastNipSql);
        ResultSet rs = pstmt.executeQuery();

        int nextNumber = 1;
        if (rs.next()) {
            String lastNip = rs.getString("nip");
            int lastNumber = Integer.parseInt(lastNip.substring(1));
            nextNumber = lastNumber + 1;
        }

        return String.format("G%03d", nextNumber);
    }

    @FXML
    void onSimpanDataClicked() {
        String nama = namaGuruField.getText();
        String password = passwordField.getText();

        if (nama.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi Nama dan Password.");
            return;
        }

        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            String newNip = generateNewNip(conn);

            String insertUserSql = "INSERT INTO users (login_id, password, role) VALUES (?, ?, 'guru')";
            PreparedStatement userPstmt = conn.prepareStatement(insertUserSql);
            userPstmt.setString(1, newNip);
            userPstmt.setString(2, password);
            userPstmt.executeUpdate();

            String insertGuruSql = "INSERT INTO guru (nip, nama_guru) VALUES (?, ?)";
            PreparedStatement guruPstmt = conn.prepareStatement(insertGuruSql);
            guruPstmt.setString(1, newNip);
            guruPstmt.setString(2, nama);
            guruPstmt.executeUpdate();

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data guru baru dengan NIP " + newNip + " telah berhasil disimpan.");
            onResetFormClicked();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan data. Kemungkinan NIP sudah ada atau terjadi kesalahan lain.");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML
    void onResetFormClicked() {
        namaGuruField.clear();
        passwordField.clear();
        nipLabel.setText("(Akan dibuat otomatis)");
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-view.fxml"));
            Parent root = loader.load();
            AdminViewController adminController = loader.getController();
            adminController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}