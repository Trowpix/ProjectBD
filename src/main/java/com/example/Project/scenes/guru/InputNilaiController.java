package com.example.Project.scenes.guru;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import com.example.Project.scenes.walikelas.WaliKelasViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InputNilaiController {

    @FXML
    private Label namaSiswaLabel;
    @FXML
    private TextField utsTextField;
    @FXML
    private TextField uasTextField;
    @FXML
    private ComboBox<String> nisComboBox;
    @FXML
    private ComboBox<String> semesterComboBox;
    @FXML
    private ComboBox<String> mapelComboBox;
    @FXML
    private ComboBox<String> kelasComboBox;
    @FXML
    private Label guruNameLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        initializeComboBox();
        guruNameLabel.setText("Guru: "+user.username);
    }

    @FXML
    void initialize() {
    }

    void initializeComboBox(){
        ObservableList<String> mapelList = FXCollections.observableArrayList();
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        semesterComboBox.getItems().addAll("1","2");
        semesterComboBox.setValue("1");
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT nama_mata_pelajaran FROM mata_pelajaran mapel JOIN jadwal_kelas jk ON mapel.id_mata_pelajaran = jk.id_mata_pelajaran WHERE jk.nip_guru = ?");
            stmt.setString(1,user.id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                mapelList.add(rs.getString("nama_mata_pelajaran"));
            }
            stmt = data.prepareStatement("SELECT DISTINCT nama_kelas FROM mata_pelajaran mapel JOIN jadwal_kelas jk ON mapel.id_mata_pelajaran = jk.id_mata_pelajaran JOIN kelas k ON k.id_kelas = jk.id_kelas WHERE jk.nip_guru = ?");
            stmt.setString(1, user.id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            mapelComboBox.setItems(mapelList);
            kelasComboBox.setItems(kelasList);
        }catch (SQLException e){
            System.out.println("Error updateNameLabelSQL");
        }
    }

    @FXML
    void onTampilkanSiswaClicked() {
        if (kelasComboBox.getValue() != null) {
            try {
                MainMenu app = MainMenu.getApplicationInstance();
                app.getPrimaryStage().setTitle("Tampilan Siswa");

                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-inputTampilkanSiswa.fxml"));
                Parent root = loader.load();

                InputTampilkanSiswa inputTampilkanSiswa = loader.getController();
                inputTampilkanSiswa.setUser(user);
                inputTampilkanSiswa.setKelas(kelasComboBox.getValue());

                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Kelas not Set");
            alert.setContentText("Please set Kelas first");
            alert.showAndWait();
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            if (user.role.equals("Wali Kelas")) {
                app.getPrimaryStage().setTitle("Wali Kelas View");

                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-walikelasView.fxml"));
                Parent root = loader.load();

                WaliKelasViewController waliKelasController = loader.getController();
                waliKelasController.setUser(user);

                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);
            }else {
                app.getPrimaryStage().setTitle("Guru View");

                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-view.fxml"));
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
    @FXML
    void onSimpanNilaiClicked() {
        int id_nilai_ujian = 0;
        if (check()){
            try(Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT id_nilai_ujian FROM nilai_ujian ORDER BY id_nilai_ujian");
                ResultSet rs = stmt.executeQuery();

                boolean fill = true;
                while (rs.next()) {
                    id_nilai_ujian++;
                    if (id_nilai_ujian != rs.getInt("id_nilai_ujian")) {
                        fill = false;
                        break;
                    }
                }
                if (fill) id_nilai_ujian++;

                stmt = data.prepareStatement("INSERT INTO nilai_ujian (id_nilai_ujian, nomor_induk_siswa, nip_guru, id_mata_pelajaran, semester, uts, uas) VALUES (?, ?, ?, (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?), ?, ?, ?)");
                stmt.setInt(1, id_nilai_ujian);
                stmt.setString(2, nisComboBox.getValue());
                stmt.setString(3,user.id);
                stmt.setString(4, mapelComboBox.getValue());
                stmt.setString(5, semesterComboBox.getValue());
                stmt.setInt(6, Integer.parseInt(utsTextField.getText()));
                stmt.setInt(7, Integer.parseInt(uasTextField.getText()));
                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Data berhasil");
                alert.setHeaderText("Data telah berhasil dimasukan");
                alert.showAndWait();
            }catch (SQLException e){
                System.out.println("Error: "+e);
            }
        }
    }
    @FXML
    void onKelasPicked() {
        ObservableList<String> nisList = FXCollections.observableArrayList();
        if (kelasComboBox.getValue()!=null){
            try (Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT nomor_induk_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?);");
                stmt.setString(1, kelasComboBox.getValue());

                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    nisList.add(rs.getString("nomor_induk_siswa"));
                }
                nisComboBox.setItems(nisList);
            }catch (SQLException e){
                System.out.println("Error updateNameLabelSQL: " + e);
            }
        }
    }
    @FXML
    void onNisPicked(){
        if (nisComboBox.getValue() != null){
            try (Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT nama_siswa FROM siswa WHERE nomor_induk_siswa = ?");
                stmt.setString(1, nisComboBox.getValue());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()){
                    namaSiswaLabel.setText(rs.getString("nama_siswa"));
                }
            }catch (SQLException e){
                System.out.println("Error updateNameLabelSQL: " + e);
            }
        }
    }
    boolean check(){
        if (uasTextField.getText() == null || utsTextField.getText() == null || nisComboBox.getValue() == null || semesterComboBox.getValue() == null || kelasComboBox.getValue() == null || mapelComboBox.getValue() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Belum Terisi");
            alert.setContentText("Data tidak dapat dimasukan karena belum terisi.");
            alert.showAndWait();
            return false;
        }
        try {
            if (Integer.parseInt(utsTextField.getText()) < 0 || Integer.parseInt(utsTextField.getText()) > 100 || Integer.parseInt(uasTextField.getText()) < 0 || Integer.parseInt(uasTextField.getText()) > 100) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Angka tidak Valid");
                alert.setContentText("Angka tidak masuk dalam batas nilai x>=0 AND x<=100");
                return false;
            }
        }catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Angka tidak Valid");
            alert.setContentText("Bukan angka itu palingan");
            return false;
        }
        try(Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM nilai_ujian WHERE nomor_induk_siswa = ? AND nip_guru = ? AND id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?) AND semester = ?");
            stmt.setString(1, nisComboBox.getValue());
            stmt.setString(2, user.id);
            stmt.setString(3, mapelComboBox.getValue());
            stmt.setString(4, semesterComboBox.getValue());


            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Data exist");
                alert.setContentText("Data sudah terisi");
                alert.showAndWait();
                return false;
            }
        }catch (SQLException e){
            System.out.println("Error: "+e);
            return false;
        }
        return true;
    }
}