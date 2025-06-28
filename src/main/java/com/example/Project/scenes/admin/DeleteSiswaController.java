package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.SiswaData;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class DeleteSiswaController {

    @FXML private TableView<SiswaData> siswaTable;
    @FXML private TableColumn<SiswaData, String> nisColumn;
    @FXML private TableColumn<SiswaData, String> namaColumn;

    private User user;
    private final ObservableList<SiswaData> siswaList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        siswaTable.setItems(siswaList);
        loadSiswaData();
    }

    private void loadSiswaData() {
        siswaList.clear();
        String sql = "SELECT nomor_induk_siswa, nama_siswa FROM siswa ORDER BY nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                siswaList.add(new SiswaData(rs.getString("nomor_induk_siswa"), rs.getString("nama_siswa")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onDeleteClicked() {
        SiswaData selectedSiswa = siswaTable.getSelectionModel().getSelectedItem();
        if (selectedSiswa == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih seorang siswa dari tabel terlebih dahulu.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Penghapusan");
        confirmation.setHeaderText("Anda yakin ingin menghapus " + selectedSiswa.getNama() + "?");
        confirmation.setContentText("Tindakan ini akan menghapus semua data siswa terkait (nilai, absensi, akun login) secara permanen.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteSiswaFromDatabase(selectedSiswa.getNis());
        }
    }

    private void deleteSiswaFromDatabase(String nis) {
        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            String[] deleteQueries = {
                    "DELETE FROM nilai_ujian WHERE nomor_induk_siswa = ?",
                    "DELETE FROM absensi WHERE nomor_induk_siswa = ?",
                    "DELETE FROM siswa_ekstrakurikuler WHERE nomor_induk_siswa = ?",
                    "DELETE FROM siswa WHERE nomor_induk_siswa = ?",
                    "DELETE FROM users WHERE login_id = ?"
            };

            for (String sql : deleteQueries) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nis);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data siswa berhasil dihapus.");
            loadSiswaData();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus data siswa dari database.");
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-deleteUser-menu.fxml"));
            Parent root = loader.load();
            DeleteUserMenuController controller = loader.getController();
            controller.setUser(user);
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