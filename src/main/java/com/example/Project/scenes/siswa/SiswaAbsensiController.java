package com.example.Project.scenes.siswa;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.Absensi;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiswaAbsensiController {

    @FXML
    private TableView<Absensi> absensiTable;
    @FXML
    private TableColumn<Absensi, Date> tanggalColumn;
    @FXML
    private TableColumn<Absensi, String> mapelColumn;
    @FXML
    private TableColumn<Absensi, String> statusColumn;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadAbsensiData();
    }

    @FXML
    void initialize() {
        tanggalColumn.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mataPelajaran"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void loadAbsensiData() {
        if (user == null || user.id == null) return;

        ObservableList<Absensi> absensiList = FXCollections.observableArrayList();
        String sql = "SELECT a.tanggal_absensi, mp.nama_mata_pelajaran, a.status_absensi " +
                "FROM absensi a " +
                "JOIN jadwal_kelas jk ON a.id_jadwal_kelas = jk.id_jadwal_kelas " +
                "JOIN mata_pelajaran mp ON jk.id_mata_pelajaran = mp.id_mata_pelajaran " +
                "WHERE a.nomor_induk_siswa = ? ORDER BY a.tanggal_absensi DESC";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                absensiList.add(new Absensi(
                        rs.getDate("tanggal_absensi"),
                        rs.getString("nama_mata_pelajaran"),
                        rs.getString("status_absensi")
                ));
            }
            absensiTable.setItems(absensiList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Siswa View");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("siswa-view.fxml"));
            Parent root = loader.load();
            SiswaViewController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}