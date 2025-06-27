package com.example.Project.scenes.siswa;

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

public class SiswaBiodataController {

    private User user;

    @FXML
    private TextField namaLengkapField;
    @FXML
    private TextField tempatLahirField;
    @FXML
    private DatePicker tanggalLahirPicker;
    @FXML
    private Label nomorIndukSiswaLabel;
    @FXML
    private ChoiceBox<String> jenisKelaminChoice;
    @FXML
    private ChoiceBox<String> agamaChoice;
    @FXML
    private TextArea alamatArea;
    @FXML
    private TextField nomorTeleponField;
    @FXML
    private ChoiceBox<String> golonganDarahChoice;

    @FXML
    private PasswordField passwordLamaField;
    @FXML
    private PasswordField passwordBaruField;
    @FXML
    private PasswordField konfirmasiPasswordBaruField;

    public void setUser(User user) {
        this.user = user;
        populateBiodata();
    }

    @FXML
    void initialize() {
        jenisKelaminChoice.getItems().addAll("L", "P");
        agamaChoice.getItems().addAll("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu");
        golonganDarahChoice.getItems().addAll("A", "B", "AB", "O");
        tanggalLahirPicker.setEditable(false);
    }

    private void populateBiodata() {
        if (user == null || user.id == null) return;
        String sql = "SELECT * FROM siswa WHERE nomor_induk_siswa = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                namaLengkapField.setText(rs.getString("nama_siswa"));
                tempatLahirField.setText(rs.getString("tempat_lahir_siswa"));
                Date dbDate = rs.getDate("tanggal_lahir_siswa");
                if (dbDate != null) {
                    tanggalLahirPicker.setValue(dbDate.toLocalDate());
                }
                nomorIndukSiswaLabel.setText(rs.getString("nomor_induk_siswa"));
                jenisKelaminChoice.setValue(rs.getString("gender_siswa"));
                agamaChoice.setValue(rs.getString("agama"));
                alamatArea.setText(rs.getString("alamat"));
                nomorTeleponField.setText(rs.getString("no_hp"));
                golonganDarahChoice.setValue(rs.getString("golongan_darah"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal Memuat Data", "Terjadi kesalahan saat mengambil data biodata.");
        }
    }

    @FXML
    void onSimpanPerubahanClicked() {
        if (user == null || user.id == null) return;
        String sql = "UPDATE siswa SET nama_siswa = ?, tempat_lahir_siswa = ?, tanggal_lahir_siswa = ?, gender_siswa = ?, agama = ?, alamat = ?, no_hp = ?, golongan_darah = ? WHERE nomor_induk_siswa = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, namaLengkapField.getText());
            pstmt.setString(2, tempatLahirField.getText());
            pstmt.setDate(3, Date.valueOf(tanggalLahirPicker.getValue()));
            pstmt.setString(4, jenisKelaminChoice.getValue());
            pstmt.setString(5, agamaChoice.getValue());
            pstmt.setString(6, alamatArea.getText());
            pstmt.setString(7, nomorTeleponField.getText());
            pstmt.setString(8, golonganDarahChoice.getValue());
            pstmt.setString(9, this.user.id);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                this.user.username = namaLengkapField.getText();
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Biodata Anda telah berhasil diperbarui.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memperbarui biodata.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan saat menyimpan biodata.");
        }
    }

    @FXML
    void onUbahPasswordClicked() {
        String passLama = passwordLamaField.getText();
        String passBaru = passwordBaruField.getText();
        String konfirmasiPass = konfirmasiPasswordBaruField.getText();

        if (passLama.isEmpty() || passBaru.isEmpty() || konfirmasiPass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Semua field password harus diisi.");
            return;
        }

        if (!passBaru.equals(konfirmasiPass)) {
            showAlert(Alert.AlertType.ERROR, "Gagal", "Password Baru dan Konfirmasi Password tidak cocok.");
            return;
        }

        try (Connection conn = MainDataSource.getConnection()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT password FROM users WHERE login_id = ?");
            checkStmt.setString(1, this.user.id);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(passLama)) {
                    PreparedStatement updateStmt = conn.prepareStatement("UPDATE users SET password = ? WHERE login_id = ?");
                    updateStmt.setString(1, passBaru);
                    updateStmt.setString(2, this.user.id);
                    updateStmt.executeUpdate();

                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Password Anda telah berhasil diubah.");
                    passwordLamaField.clear();
                    passwordBaruField.clear();
                    konfirmasiPasswordBaruField.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Password Lama yang Anda masukkan salah.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Kesalahan Database", "Terjadi kesalahan saat mengubah password.");
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa View");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("siswa-view.fxml"));
            Parent root = loader.load();
            SiswaViewController siswaViewController = loader.getController();
            siswaViewController.setUser(user);
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