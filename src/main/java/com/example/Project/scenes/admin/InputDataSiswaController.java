package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;

public class InputDataSiswaController {
    @FXML
    private TextField namaLengkapField;
    @FXML
    private TextField tempatLahirField;
    @FXML
    private DatePicker tanggalLahirPicker;
    @FXML
    private ChoiceBox<String> jenisKelaminChoice;
    @FXML
    private ChoiceBox<String> agamaChoice;
    @FXML
    private TextArea alamatField;
    @FXML
    private TextField nomorTeleponField;
    @FXML
    private ChoiceBox<String> golonganDarahChoice;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        jenisKelaminChoice.getItems().addAll("L","P");
        agamaChoice.getItems().addAll("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu");
        golonganDarahChoice.getItems().addAll("A", "B", "AB", "O");
        tanggalLahirPicker.setEditable(false);
    }

    @FXML
    void onResetFormClicked() {
        namaLengkapField.clear();
        tempatLahirField.clear();
        tanggalLahirPicker.setValue(null);
        jenisKelaminChoice.setValue(null);
        agamaChoice.setValue(null);
        alamatField.clear();
        nomorTeleponField.clear();
        golonganDarahChoice.setValue(null);
    }

    @FXML
    void onSimpanDataClicked() {
        if (!cekInputValid()) {
            return;
        }

        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            String newNis = generateNewNis(conn);
            String newPassword = generateNewPassword(conn);

            String userSql = "INSERT INTO users (login_id, password, role) VALUES (?, ?, 'siswa')";
            PreparedStatement userStmt = conn.prepareStatement(userSql);
            userStmt.setString(1, newNis);
            userStmt.setString(2, newPassword);
            userStmt.executeUpdate();

            String siswaSql = "INSERT INTO siswa (nomor_induk_siswa, nama_siswa, tempat_lahir_siswa, tanggal_lahir_siswa, gender_siswa, agama, alamat, no_hp, golongan_darah) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement siswaStmt = conn.prepareStatement(siswaSql);
            siswaStmt.setString(1, newNis);
            siswaStmt.setString(2, namaLengkapField.getText());
            siswaStmt.setString(3, tempatLahirField.getText());
            siswaStmt.setDate(4, Date.valueOf(tanggalLahirPicker.getValue()));
            siswaStmt.setString(5, jenisKelaminChoice.getValue());
            siswaStmt.setString(6, agamaChoice.getValue());
            siswaStmt.setString(7, alamatField.getText());
            siswaStmt.setString(8, nomorTeleponField.getText());
            siswaStmt.setString(9, golonganDarahChoice.getValue());
            siswaStmt.executeUpdate();

            conn.commit();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Data Berhasil Disimpan");
            alert.setHeaderText("Siswa baru berhasil ditambahkan.");
            alert.setContentText("NIS: " + newNis + "\nPassword Awal: " + newPassword);
            alert.showAndWait();

            onResetFormClicked();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan data siswa.");
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

    private String generateNewNis(Connection conn) throws SQLException {
        String lastNisSql = "SELECT nomor_induk_siswa FROM siswa WHERE nomor_induk_siswa LIKE 'SD%' ORDER BY CAST(SUBSTRING(nomor_induk_siswa, 3) AS INTEGER) DESC LIMIT 1";
        PreparedStatement pstmt = conn.prepareStatement(lastNisSql);
        ResultSet rs = pstmt.executeQuery();
        int nextNumber = 1;
        if (rs.next()) {
            int lastNumber = Integer.parseInt(rs.getString("nomor_induk_siswa").substring(2));
            nextNumber = lastNumber + 1;
        }
        return String.format("SD%03d", nextNumber);
    }

    private String generateNewPassword(Connection conn) throws SQLException {
        String lastPassSql = "SELECT password FROM users WHERE password LIKE 'sp%' ORDER BY CAST(SUBSTRING(password, 3) AS INTEGER) DESC LIMIT 1";
        PreparedStatement pstmt = conn.prepareStatement(lastPassSql);
        ResultSet rs = pstmt.executeQuery();
        int nextNumber = 1;
        if (rs.next()) {
            int lastNumber = Integer.parseInt(rs.getString("password").substring(2));
            nextNumber = lastNumber + 1;
        }
        return String.format("sp%03d", nextNumber);
    }

    boolean cekInputValid(){
        if (namaLengkapField.getText() == null || namaLengkapField.getText().isEmpty() ||
                tempatLahirField.getText() == null || tempatLahirField.getText().isEmpty() ||
                tanggalLahirPicker.getValue() == null ||
                jenisKelaminChoice.getValue() == null ||
                agamaChoice.getValue() == null ||
                alamatField.getText() == null || alamatField.getText().isEmpty()) {

            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua field yang bertanda *.");
            return false;
        }
        return true;
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
        }catch (IOException e){
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