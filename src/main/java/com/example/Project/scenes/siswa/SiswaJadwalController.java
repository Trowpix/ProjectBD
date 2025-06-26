package com.example.Project.scenes.siswa;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableViewJadwal;
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
import java.sql.*;
import java.util.ArrayList;

public class SiswaJadwalController {

    @FXML
    private TableView<TableViewJadwal> scheduleTable;
    @FXML
    private TableColumn<TableViewJadwal, String> waktuColumn = new TableColumn<>();
    @FXML
    private TableColumn<TableViewJadwal, String> seninColumn= new TableColumn<>();
    @FXML
    private TableColumn<TableViewJadwal, String> selasaColumn= new TableColumn<>();
    @FXML
    private TableColumn<TableViewJadwal, String> rabuColumn= new TableColumn<>();
    @FXML
    private TableColumn<TableViewJadwal, String> kamisColumn= new TableColumn<>();
    @FXML
    private TableColumn<TableViewJadwal, String> jumatColumn;


    private User user = new User();

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
            if (user.id != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT jam_jadwal_kelas FROM jadwal_kelas ORDER BY jam_jadwal_kelas");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    jam.add(rs.getTimestamp("jam_jadwal_kelas"));
                }

                for (Timestamp timestamp : jam) {
                    String senin = null;
                    String selasa = null;
                    String rabu = null;
                    String kamis = null;
                    String jumat = null;
                    Timestamp end;
                    if ((timestamp.toString().substring(11).equals("09:15:00.0")) || (timestamp.toString().substring(11).equals("11:45:00.0"))){
                        senin = "Istirahat";
                        selasa = "Istirahat";
                        rabu = "Istirahat";
                        kamis = "Istirahat";
                        jumat = "Istirahat";
                        end = new Timestamp(timestamp.getTime()+1000*60*15);
                    }
                    else {
                        stmt = data.prepareStatement("SELECT jk.hari_jadwal_kelas, nama_mata_pelajaran FROM jadwal_kelas jk JOIN mata_pelajaran mapel ON jk.id_mata_pelajaran =  mapel.id_mata_pelajaran WHERE jk.id_kelas = (SELECT id_kelas FROM siswa WHERE nomor_induk_siswa = ?) AND jam_jadwal_kelas = ? ORDER BY hari_jadwal_kelas;");
                        stmt.setString(1, user.id);
                        stmt.setTimestamp(2, timestamp);


                        rs = stmt.executeQuery();
                        while (rs.next()) {
                            String hari = rs.getString("hari_jadwal_kelas").toLowerCase();
                            switch (hari) {
                                case "senin" -> senin = rs.getString("nama_mata_pelajaran");
                                case "selasa" -> selasa = rs.getString("nama_mata_pelajaran");
                                case "rabu" -> rabu = rs.getString("nama_mata_pelajaran");
                                case "kamis" -> kamis = rs.getString("nama_mata_pelajaran");
                                case "jumat" -> jumat = rs.getString("nama_mata_pelajaran");
                            }
                        }
                        end = new Timestamp(timestamp.getTime()+1000*60*45);
                    }
                    jadwal.add(new TableViewJadwal(timestamp,end, senin, selasa, rabu, kamis, jumat));
                }
                scheduleTable.setItems(jadwal);
            }
        }catch (SQLException e){
            System.out.println("Error updateNameLabelSQL: "+e);
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
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
