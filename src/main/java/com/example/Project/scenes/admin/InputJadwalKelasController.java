package com.example.Project.scenes.admin;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputJadwalKelasController {

    @FXML private ChoiceBox<String> kelasChoice;
    @FXML private ChoiceBox<String> hariChoice;
    @FXML private ChoiceBox<String> mapel1, mapel2, mapel3, mapel4, mapel5, mapel6, mapel7, mapel8;
    @FXML private ChoiceBox<String> guru1, guru2, guru3, guru4, guru5, guru6, guru7, guru8;

    private User user;
    private List<ChoiceBox<String>> mapelBoxes;
    private List<ChoiceBox<String>> guruBoxes;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        mapelBoxes = Arrays.asList(mapel1, mapel2, mapel3, mapel4, mapel5, mapel6, mapel7, mapel8);
        guruBoxes = Arrays.asList(guru1, guru2, guru3, guru4, guru5, guru6, guru7, guru8);

        ObservableList<String> kelasList = FXCollections.observableArrayList();
        ObservableList<String> mapelList = FXCollections.observableArrayList();
        ObservableList<String> guruList = FXCollections.observableArrayList();

        try(Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT nama_kelas FROM kelas ORDER BY nama_kelas");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                kelasList.add(rs.getString("nama_kelas"));
            }

            stmt = data.prepareStatement("SELECT DISTINCT nama_mata_pelajaran FROM mata_pelajaran ORDER BY nama_mata_pelajaran");
            rs = stmt.executeQuery();
            while (rs.next()){
                mapelList.add(rs.getString("nama_mata_pelajaran"));
            }

            stmt = data.prepareStatement("SELECT DISTINCT nama_guru FROM guru ORDER BY nama_guru");
            rs = stmt.executeQuery();
            while (rs.next()){
                guruList.add(rs.getString("nama_guru"));
            }

            kelasChoice.setItems(kelasList);
            hariChoice.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat");

            for(ChoiceBox<String> cb : mapelBoxes) { cb.setItems(mapelList); }
            for(ChoiceBox<String> cb : guruBoxes) { cb.setItems(guruList); }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    void onFilterJadwalClicked() {
        if (kelasChoice.getValue() == null || hariChoice.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih Kelas dan Hari terlebih dahulu.");
            return;
        }
        onResetClicked();
        String sql = "SELECT jk.jam_jadwal_kelas, mp.nama_mata_pelajaran, g.nama_guru " +
                "FROM jadwal_kelas jk " +
                "JOIN mata_pelajaran mp ON jk.id_mata_pelajaran = mp.id_mata_pelajaran " +
                "JOIN guru g ON jk.nip_guru = g.nip " +
                "WHERE jk.id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND jk.hari_jadwal_kelas = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, kelasChoice.getValue());
            pstmt.setString(2, hariChoice.getValue());
            ResultSet rs = pstmt.executeQuery();

            List<String> jamPelajaran = Arrays.asList("07:00:00", "07:45:00", "08:30:00", "09:30:00", "10:15:00", "11:00:00", "12:00:00", "12:45:00");

            while(rs.next()) {
                String jamDb = rs.getString("jam_jadwal_kelas");
                int index = jamPelajaran.indexOf(jamDb);

                if(index != -1) {
                    mapelBoxes.get(index).setValue(rs.getString("nama_mata_pelajaran"));
                    guruBoxes.get(index).setValue(rs.getString("nama_guru"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal memuat jadwal yang sudah ada.");
        }
    }

    @FXML
    void onSimpanClicked() {
        if (kelasChoice.getValue() == null || hariChoice.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih Kelas dan Hari terlebih dahulu.");
            return;
        }

        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM jadwal_kelas WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND hari_jadwal_kelas = ?");
            deleteStmt.setString(1, kelasChoice.getValue());
            deleteStmt.setString(2, hariChoice.getValue());
            deleteStmt.executeUpdate();

            List<String> jamPelajaran = Arrays.asList(
                    "07:00:00", "07:45:00", "08:30:00", "09:30:00",
                    "10:15:00", "11:00:00", "12:00:00", "12:45:00"
            );

            String insertSql = "INSERT INTO jadwal_kelas (id_kelas, hari_jadwal_kelas, jam_jadwal_kelas, nip_guru, id_mata_pelajaran) VALUES ((SELECT id_kelas FROM kelas WHERE nama_kelas = ?), ?, ?, (SELECT nip FROM guru WHERE nama_guru = ?), (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?))";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            for (int i = 0; i < mapelBoxes.size(); i++) {
                String mapel = mapelBoxes.get(i).getValue();
                String guru = guruBoxes.get(i).getValue();

                if (mapel != null && guru != null && !mapel.isEmpty() && !guru.isEmpty()) {
                    insertStmt.setString(1, kelasChoice.getValue());
                    insertStmt.setString(2, hariChoice.getValue());
                    insertStmt.setTime(3, java.sql.Time.valueOf(jamPelajaran.get(i)));
                    insertStmt.setString(4, guru);
                    insertStmt.setString(5, mapel);
                    insertStmt.addBatch();
                }
            }

            insertStmt.executeBatch();
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Jadwal kelas berhasil disimpan.");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat menyimpan jadwal.");
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    void onResetClicked() {
        for (ChoiceBox<String> cb : mapelBoxes) {
            cb.setValue(null);
        }
        for (ChoiceBox<String> cb : guruBoxes) {
            cb.setValue(null);
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/admin/admin-view.fxml"));
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