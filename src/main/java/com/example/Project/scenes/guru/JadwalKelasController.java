package com.example.Project.scenes.guru;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableViewJadwal;
import com.example.Project.dtos.User;
import com.example.Project.scenes.walikelas.WaliKelasViewController;
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
import java.sql.*;
import java.util.ArrayList;

public class JadwalKelasController {
    @FXML
    private TableView<TableViewJadwal> scheduleTable;
    @FXML
    private TableColumn<TableViewJadwal, String> waktuColumn;
    @FXML
    private TableColumn<TableViewJadwal, String> seninColumn;
    @FXML
    private TableColumn<TableViewJadwal, String> selasaColumn;
    @FXML
    private TableColumn<TableViewJadwal, String> rabuColumn;
    @FXML
    private TableColumn<TableViewJadwal, String> kamisColumn;
    @FXML
    private TableColumn<TableViewJadwal, String> jumatColumn;

    private User user;

    public void setUser(User user) {
        this.user = user;
        update();
    }

    @FXML
    void initialize(){
        waktuColumn.setCellValueFactory(new PropertyValueFactory<>("waktu"));
        seninColumn.setCellValueFactory(new PropertyValueFactory<>("senin"));
        selasaColumn.setCellValueFactory(new PropertyValueFactory<>("selasa"));
        rabuColumn.setCellValueFactory(new PropertyValueFactory<>("rabu"));
        kamisColumn.setCellValueFactory(new PropertyValueFactory<>("kamis"));
        jumatColumn.setCellValueFactory(new PropertyValueFactory<>("jumat"));
    }

    void update(){
        ObservableList<TableViewJadwal> jadwal = FXCollections.observableArrayList();
        ArrayList<Timestamp> jam = new ArrayList<>();
        try (Connection data = MainDataSource.getConnection()){
            if (user != null && user.id != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT jam_jadwal_kelas FROM jadwal_kelas ORDER BY jam_jadwal_kelas");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    jam.add(rs.getTimestamp("jam_jadwal_kelas"));
                }

                for (Timestamp timestamp : jam) {
                    String senin = null, selasa = null, rabu = null, kamis = null, jumat = null;
                    Timestamp end;

                    if ((timestamp.toString().contains("09:15:00")) || (timestamp.toString().contains("11:45:00"))){
                        senin = selasa = rabu = kamis = jumat = "Istirahat";
                        end = new Timestamp(timestamp.getTime() + (15 * 60 * 1000));
                    } else {
                        stmt = data.prepareStatement("SELECT jk.hari_jadwal_kelas, k.nama_kelas FROM jadwal_kelas jk JOIN kelas k ON jk.id_kelas = k.id_kelas WHERE jk.nip_guru = ? AND jk.jam_jadwal_kelas = ?");
                        stmt.setString(1, user.id);
                        stmt.setTimestamp(2, timestamp);
                        rs = stmt.executeQuery();

                        while (rs.next()) {
                            String hari = rs.getString("hari_jadwal_kelas").toLowerCase();
                            String namaKelas = rs.getString("nama_kelas");
                            switch (hari) {
                                case "senin" -> senin = namaKelas;
                                case "selasa" -> selasa = namaKelas;
                                case "rabu" -> rabu = namaKelas;
                                case "kamis" -> kamis = namaKelas;
                                case "jumat" -> jumat = namaKelas;
                            }
                        }
                        end = new Timestamp(timestamp.getTime() + (45 * 60 * 1000));
                    }
                    jadwal.add(new TableViewJadwal(timestamp, end, senin, selasa, rabu, kamis, jumat));
                }
                scheduleTable.setItems(jadwal);
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            if (user.role.equals("Wali Kelas")) {
                app.getPrimaryStage().setTitle("Wali Kelas View");
                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-walikelasView.fxml"));
                Parent root = loader.load();
                WaliKelasViewController waliKelasController = loader.getController();
                waliKelasController.setUser(user);
                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);
            } else {
                app.getPrimaryStage().setTitle("Guru View");
                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-view.fxml"));
                Parent root = loader.load();
                GuruViewController guruController = loader.getController();
                guruController.setUser(user);
                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}