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
import javafx.scene.control.ChoiceBox;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class InputJadwalKelasController {

    @FXML
    private ChoiceBox<String> kelasChoice;
    @FXML
    private ChoiceBox<String> hariChoice;

    @FXML
    private ChoiceBox<String> mapel1;
    @FXML
    private ChoiceBox<String> guru1;

    @FXML
    private ChoiceBox<String> mapel2;
    @FXML
    private ChoiceBox<String> guru2;

    @FXML
    private ChoiceBox<String> mapel3;
    @FXML
    private ChoiceBox<String> guru3;

    @FXML
    private ChoiceBox<String> mapel4;
    @FXML
    private ChoiceBox<String> guru4;

    @FXML
    private ChoiceBox<String> mapel5;
    @FXML
    private ChoiceBox<String> guru5;

    @FXML
    private ChoiceBox<String> mapel6;
    @FXML
    private ChoiceBox<String> guru6;

    @FXML
    private ChoiceBox<String> mapel7;
    @FXML
    private ChoiceBox<String> guru7;

    @FXML
    private ChoiceBox<String> mapel8;
    @FXML
    private ChoiceBox<String> guru8;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
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
            kelasChoice.setItems(kelasList);
            hariChoice.getItems().addAll("Senin", "Selasa", "Rabu", "Kamis", "Jumat");
            mapel1.setItems(mapelList);
            mapel2.setItems(mapelList);
            mapel3.setItems(mapelList);
            mapel4.setItems(mapelList);
            mapel5.setItems(mapelList);
            mapel6.setItems(mapelList);
            mapel7.setItems(mapelList);
            mapel8.setItems(mapelList);

            stmt = data.prepareStatement("SELECT DISTINCT nama_guru FROM guru ORDER BY nama_guru");
            rs = stmt.executeQuery();
            while (rs.next()){
                guruList.add(rs.getString("nama_guru"));
            }

            guru1.setItems(guruList);
            guru2.setItems(guruList);
            guru3.setItems(guruList);
            guru4.setItems(guruList);
            guru5.setItems(guruList);
            guru6.setItems(guruList);
            guru7.setItems(guruList);
            guru8.setItems(guruList);
        }catch (SQLException e){
            System.out.println("Error initialize: "+e);
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");

            // Load fxml and set the scene
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

    @FXML
    void onFilterJadwalClicked() {
        try(Connection data = MainDataSource.getConnection()) {
            if (kelasChoice.getValue() != null && hariChoice.getValue() != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM jadwal_kelas jk JOIN mata_pelajaran mp ON jk.id_mata_pelajaran = mp.id_mata_pelajaran JOIN guru g ON jk.nip_guru = g.nip WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND hari_jadwal_kelas = ?;");
                stmt.setString(1, kelasChoice.getValue());
                stmt.setString(2, hariChoice.getValue());

                boolean[] fill = new boolean[8];

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String jam = rs.getString("jam_jadwal_kelas");
                    switch (jam){
                        case "07:00:00" :
                            mapel1.setValue(rs.getString("nama_mata_pelajaran"));
                            guru1.setValue(rs.getString("nama_guru"));
                            fill[0] = true;
                            break;
                        case "07:45:00" :
                            mapel2.setValue(rs.getString("nama_mata_pelajaran"));
                            guru2.setValue(rs.getString("nama_guru"));
                            fill[1] = true;
                            break;
                        case "08:30:00" :
                            mapel3.setValue(rs.getString("nama_mata_pelajaran"));
                            guru3.setValue(rs.getString("nama_guru"));
                            fill[2] = true;
                            break;
                        case "09:30:00" :
                            mapel4.setValue(rs.getString("nama_mata_pelajaran"));
                            guru4.setValue(rs.getString("nama_guru"));
                            fill[3] = true;
                            break;
                        case "10:15:00" :
                            mapel5.setValue(rs.getString("nama_mata_pelajaran"));
                            guru5.setValue(rs.getString("nama_guru"));
                            fill[4] = true;
                            break;
                        case "11:00:00" :
                            mapel6.setValue(rs.getString("nama_mata_pelajaran"));
                            guru6.setValue(rs.getString("nama_guru"));
                            fill[5] = true;
                            break;
                        case "12:00:00" :
                            mapel7.setValue(rs.getString("nama_mata_pelajaran"));
                            guru7.setValue(rs.getString("nama_guru"));
                            fill[6] = true;
                            break;
                        case "12:45:00" :
                            mapel8.setValue(rs.getString("nama_mata_pelajaran"));
                            guru8.setValue(rs.getString("nama_guru"));
                            fill[7] = true;
                            break;
                    }
                }
                if (!fill[0]){
                    mapel1.setValue(null);
                    guru1.setValue(null);
                }if (!fill[1]){
                    mapel2.setValue(null);
                    guru2.setValue(null);
                }if (!fill[2]){
                    mapel3.setValue(null);
                    guru3.setValue(null);
                }if (!fill[3]){
                    mapel4.setValue(null);
                    guru4.setValue(null);
                }if (!fill[4]){
                    mapel5.setValue(null);
                    guru5.setValue(null);
                }if (!fill[5]){
                    mapel6.setValue(null);
                    guru6.setValue(null);
                }if (!fill[6]){
                    mapel7.setValue(null);
                    guru7.setValue(null);
                }if (!fill[7]){
                    mapel8.setValue(null);
                    guru8.setValue(null);
                }
            }
        } catch (SQLException e) {
            System.out.println("Update Failed");
        }
    }

    @FXML
    void onSimpanClicked() {
        ArrayList<ChoiceBox<String>> mapel = new ArrayList<>();
        mapel.add(mapel1);
        mapel.add(mapel2);
        mapel.add(mapel3);
        mapel.add(mapel4);
        mapel.add(mapel5);
        mapel.add(mapel6);
        mapel.add(mapel7);
        mapel.add(mapel8);
        ArrayList<ChoiceBox<String>> guru = new ArrayList<>();
        guru.add(guru1);
        guru.add(guru2);
        guru.add(guru3);
        guru.add(guru4);
        guru.add(guru5);
        guru.add(guru6);
        guru.add(guru7);
        guru.add(guru8);
        ArrayList<Timestamp> jam = new ArrayList<>();
        try (Connection data = MainDataSource.getConnection()) {
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT jam_jadwal_kelas FROM jadwal_kelas ORDER BY jam_jadwal_kelas");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                jam.add(rs.getTimestamp("jam_jadwal_kelas"));
            }

            for (int i = 0; i < guru.size(); i++) {
                stmt = data.prepareStatement("DELETE FROM jadwal_kelas WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND hari_jadwal_kelas = ? AND jam_jadwal_kelas = ?");
                stmt.setString(1, kelasChoice.getValue());
                stmt.setString(2, hariChoice.getValue());
                stmt.setTimestamp(3, jam.get(i));
                stmt.executeUpdate();

                stmt = data.prepareStatement("SELECT id_jadwal_kelas FROM jadwal_kelas ORDER BY id_jadwal_kelas");
                rs = stmt.executeQuery();
                int id_jadwal = 0;
                boolean fill = true;
                while (rs.next()) {
                    id_jadwal++;
                    if (id_jadwal != rs.getInt("id_jadwal_kelas")) {
                        fill = false;
                        break;
                    }
                }
                if (fill) id_jadwal++;

                stmt = data.prepareStatement("INSERT INTO jadwal_kelas (id_jadwal_kelas, id_kelas, hari_jadwal_kelas, jam_jadwal_kelas, nip_guru, id_mata_pelajaran) VALUES (?,(SELECT id_kelas FROM kelas WHERE nama_kelas = ?),?,?,(SELECT nip FROM guru WHERE nama_guru = ?),(SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?));");
                stmt.setInt(1, id_jadwal);
                stmt.setString(2, kelasChoice.getValue());
                stmt.setString(3, hariChoice.getValue());
                stmt.setTimestamp(4, jam.get(i));
                stmt.setString(5, guru.get(i).getValue());
                stmt.setString(6, mapel.get(i).getValue());

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Simpan Failed: "+e);
        }
    }

    @FXML
    void onResetClicked() {
        kelasChoice.setValue(null);
        hariChoice.setValue(null);

        mapel1.setValue(null);
        guru1.setValue(null);

        mapel2.setValue(null);
        guru2.setValue(null);

        mapel3.setValue(null);
        guru3.setValue(null);

        mapel4.setValue(null);
        guru4.setValue(null);

        mapel5.setValue(null);
        guru5.setValue(null);

        mapel6.setValue(null);
        guru6.setValue(null);

        mapel7.setValue(null);
        guru7.setValue(null);

        mapel8.setValue(null);
        guru8.setValue(null);
    }
}
