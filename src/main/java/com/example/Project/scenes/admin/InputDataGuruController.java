package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InputDataGuruController {

    @FXML
    private TextField nipField;
    @FXML
    private TextField namaGuruField;
    @FXML
    private PasswordField passwordField;

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void onSimpanDataClicked() {
        String nip = nipField.getText();
        String nama = namaGuruField.getText();
        String password = passwordField.getText();

        if (nip.isEmpty() || nama.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua field (NIP, Nama, dan Password).");
            return;
        }

        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT 1 FROM users WHERE login_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, nip);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Data Duplikat", "NIP " + nip + " sudah terdaftar di sistem.");
                conn.rollback();
                return;
            }

            String insertUserSql = "INSERT INTO users (login_id, password, role) VALUES (?, ?, 'guru')";
            PreparedStatement userPstmt = conn.prepareStatement(insertUserSql);
            userPstmt.setString(1, nip);
            userPstmt.setString(2, password);
            userPstmt.executeUpdate();

            String insertGuruSql = "INSERT INTO guru (nip, nama_guru) VALUES (?, ?)";
            PreparedStatement guruPstmt = conn.prepareStatement(insertGuruSql);
            guruPstmt.setString(1, nip);
            guruPstmt.setString(2, nama);
            guruPstmt.executeUpdate();

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data guru baru telah berhasil disimpan.");
            onResetFormClicked();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan data ke database.");
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
        nipField.clear();
        namaGuruField.clear();
        passwordField.clear();
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