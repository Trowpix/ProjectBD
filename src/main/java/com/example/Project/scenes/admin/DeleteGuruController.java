package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.GuruData;
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

public class DeleteGuruController {

    @FXML private TableView<GuruData> guruTable;
    @FXML private TableColumn<GuruData, String> nipColumn;
    @FXML private TableColumn<GuruData, String> namaColumn;

    private User user;
    private final ObservableList<GuruData> guruList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        nipColumn.setCellValueFactory(new PropertyValueFactory<>("nip"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        guruTable.setItems(guruList);
        loadGuruData();
    }

    private void loadGuruData() {
        guruList.clear();
        String sql = "SELECT nip, nama_guru FROM guru ORDER BY nama_guru";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                guruList.add(new GuruData(rs.getString("nip"), rs.getString("nama_guru")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onDeleteClicked() {
        GuruData selectedGuru = guruTable.getSelectionModel().getSelectedItem();
        if (selectedGuru == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih seorang guru dari tabel terlebih dahulu.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Penghapusan");
        confirmation.setHeaderText("Anda yakin ingin menghapus " + selectedGuru.getNama() + "?");
        confirmation.setContentText("Tindakan ini akan menghapus semua data guru terkait.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteGuruFromDatabase(selectedGuru.getNip());
        }
    }

    private void deleteGuruFromDatabase(String nip) {
        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            String[] deleteQueries = {
                    "DELETE FROM nilai_ujian WHERE nip_guru = ?",
                    "DELETE FROM wali_kelas WHERE nip_guru = ?",
                    "DELETE FROM guru WHERE nip = ?",
                    "DELETE FROM users WHERE login_id = ?"
            };

            String updateJadwalSql = "UPDATE jadwal_kelas SET nip_guru = NULL WHERE nip_guru = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateJadwalSql)) {
                pstmt.setString(1, nip);
                pstmt.executeUpdate();
            }

            for (String sql : deleteQueries) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, nip);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data guru berhasil dihapus.");
            loadGuruData();

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menghapus data guru dari database.");
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-deleteUser-menu.fxml"));
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