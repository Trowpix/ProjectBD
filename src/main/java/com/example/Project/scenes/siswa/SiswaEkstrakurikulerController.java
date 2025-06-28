package com.example.Project.scenes.siswa;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableTampilanEkstrakulikurel;
import com.example.Project.dtos.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiswaEkstrakurikulerController {

    @FXML
    private TableView<TableTampilanEkstrakulikurel> tableViewEkstrakurikuler;
    @FXML
    private TableColumn<TableTampilanEkstrakulikurel, String> kolomNamaEskul;
    @FXML
    private TableColumn<TableTampilanEkstrakulikurel, String> kolomTanggal;
    @FXML
    private TableColumn<TableTampilanEkstrakulikurel, String> kolomPembina;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadExtracurricularData();
    }

    private void loadExtracurricularData() {
        ObservableList<TableTampilanEkstrakulikurel> dataList = FXCollections.observableArrayList();
        String sql = "SELECT e.nama_ekskul, e.jadwal, e.nama_pembina " +
                "FROM ekstrakurikuler e " +
                "JOIN siswa_ekstrakurikuler se ON e.id_ekskul = se.id_ekskul " +
                "WHERE se.nomor_induk_siswa = ?";

        if (user != null && user.id != null) {
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, user.id);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    SimpleStringProperty nama = new SimpleStringProperty(rs.getString("nama_ekskul"));
                    SimpleStringProperty jadwal = new SimpleStringProperty(rs.getString("jadwal"));
                    SimpleStringProperty pembina = new SimpleStringProperty(rs.getString("nama_pembina"));
                    dataList.add(new TableTampilanEkstrakulikurel(nama, jadwal, pembina));
                }

            } catch (SQLException e) {
                System.err.println("Error loading extracurricular data: " + e.getMessage());
                e.printStackTrace();
            }
        }

        tableViewEkstrakurikuler.setItems(dataList);
    }

    @FXML
    void onBackToDashboardClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Dashboard Siswa");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-view.fxml"));
            Parent root = loader.load();

            SiswaViewController controller = loader.getController();
            controller.setUser(this.user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            System.err.println("Gagal kembali ke dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
