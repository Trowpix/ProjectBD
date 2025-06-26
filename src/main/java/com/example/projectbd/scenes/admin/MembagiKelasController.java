package com.example.projectbd.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MembagiKelasController {


    @FXML
    private ChoiceBox<String> kelasChoiceBox;
    @FXML
    private ListView<String> unassignedStudentsList;
    @FXML
    private ChoiceBox<String> waliKelasChoice;
    @FXML
    private ListView<String> assignedStudentsList;
    @FXML
    private Label kelasLabel;
    @FXML
    private Label jumlahSiswaLabel;
    @FXML
    private Label waliKelasLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        initializeChoiceBox();
        update();
    }

    @FXML
    void initialize() {
        assignedStudentsList.setItems(null);
    }

    void update(){
        ObservableList<String> unassigned = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE id_kelas IS NULL");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                unassigned.add("("+rs.getString("nomor_induk_siswa")+") - "+rs.getString("nama_siswa"));
            }
            unassignedStudentsList.setItems(unassigned);
        }catch (SQLException e){
            System.out.println("Error update: "+e);
        }
    }

    void initializeChoiceBox(){
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        ObservableList<String> waliKelasList = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT nama_kelas FROM kelas");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            kelasChoiceBox.setItems(kelasList);
            stmt = data.prepareStatement("SELECT nama_guru FROM guru WHERE nip IN (SELECT nip_guru FROM wali_kelas)");
            rs = stmt.executeQuery();
            while (rs.next()){
                waliKelasList.add(rs.getString("nama_guru"));
            }
            waliKelasChoice.setItems(waliKelasList);
        }catch (SQLException e){
            System.out.println("Error initializeChoiceBox: "+e);
        }
    }



    @FXML
    void onKembaliClicked() {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");

            // Load fxml and set the scene
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
            Parent root = loader.load();
            AdminViewController adminController = loader.getController();
            adminController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    String getNis(String s){
        int end = 1;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ')') end=i;
        }
        return s.substring(1,end);
    }

    @FXML
    void onTampilkanDataClicked(){
        if (kelasChoiceBox.getValue()!= null) {
            String kelas = kelasChoiceBox.getValue();
            onResetClicked();
            kelasChoiceBox.setValue(kelas);
            ObservableList<String> assigned = FXCollections.observableArrayList();
            kelasLabel.setText(kelasChoiceBox.getValue());
            try (Connection data = MainDataSource.getConnection()) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)");
                stmt.setString(1, kelasChoiceBox.getValue());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    assigned.add("(" + rs.getString("nomor_induk_siswa") + ") - " + rs.getString("nama_siswa"));
                }
                assignedStudentsList.setItems(assigned);
                stmt = data.prepareStatement("SELECT nama_guru FROM guru WHERE nip = (SELECT nip_guru FROM wali_kelas WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?))");
                stmt.setString(1, kelasChoiceBox.getValue());
                rs = stmt.executeQuery();
                if (rs.next()){
                    waliKelasLabel.setText(rs.getString("nama_guru"));
                    waliKelasChoice.setValue(rs.getString("nama_guru"));
                }
                stmt = data.prepareStatement("SELECT COUNT(nomor_induk_siswa) FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)");
                stmt.setString(1, kelasChoiceBox.getValue());
                rs = stmt.executeQuery();
                if (rs.next()){
                    jumlahSiswaLabel.setText(String.valueOf(rs.getInt(1)));
                }

            } catch (SQLException e) {
                System.out.println("Error tampilkan data: " + e);
            }
        }
    }

    @FXML
    void onSimpanClicked() {
        if (kelasChoiceBox.getValue() != null && assignedStudentsList.getItems() != null && unassignedStudentsList.getItems() != null){
            try (Connection data = MainDataSource.getConnection()) {
                PreparedStatement stmt;
                for (int i = 0; i < assignedStudentsList.getItems().size(); i++) {
                    String nis = getNis(assignedStudentsList.getItems().get(i));
                    stmt = data.prepareStatement("UPDATE siswa SET id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) WHERE nomor_induk_siswa = ?");
                    stmt.setString(1, kelasChoiceBox.getValue());
                    stmt.setString(2, nis);
                    stmt.executeUpdate();
                }
                for (int i = 0; i < unassignedStudentsList.getItems().size(); i++) {
                    String nis = getNis(unassignedStudentsList.getItems().get(i));
                    stmt = data.prepareStatement("UPDATE siswa SET id_kelas = null WHERE nomor_induk_siswa = ?");
                    stmt.setString(1, nis);
                    stmt.executeUpdate();
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Data berhasil");
                alert.setHeaderText("Data telah berhasil dimasukan");
                alert.showAndWait();

            } catch (SQLException e) {
                System.out.println("Error SimpanClicked: " + e);
            }
        }
    }

    @FXML
    void onResetClicked() {
        update();
        kelasChoiceBox.setValue(null);
        waliKelasChoice.setValue(null);
        assignedStudentsList.setItems(null);
        kelasLabel.setText("Nama Kelas");
        jumlahSiswaLabel.setText("0");
        waliKelasLabel.setText("Belum Dipilih");
    }

    @FXML
    void onTambahkanClicked() {
        if (unassignedStudentsList.getSelectionModel().getSelectedItem() != null && assignedStudentsList.getItems() != null) {
            assignedStudentsList.getItems().add(unassignedStudentsList.getSelectionModel().getSelectedItem());
            unassignedStudentsList.getItems().remove(unassignedStudentsList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    void onKembalikanClicked() {
        if (assignedStudentsList.getSelectionModel().getSelectedItem() != null){
            unassignedStudentsList.getItems().add(assignedStudentsList.getSelectionModel().getSelectedItem());
            assignedStudentsList.getItems().remove(assignedStudentsList.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
     void onSimpanWaliKelasClicked() {
        if (waliKelasChoice.getValue()!= null) {
            try (Connection data = MainDataSource.getConnection()) {
                PreparedStatement stmt = data.prepareStatement("UPDATE wali_kelas SET id_kelas = NULL WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?);\n" +
                        "UPDATE wali_kelas SET id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) WHERE nip_guru = (SELECT nip FROM guru WHERE nama_guru = ?);");
                stmt.setString(1, kelasChoiceBox.getValue());
                stmt.setString(2, kelasChoiceBox.getValue());
                stmt.setString(3, waliKelasChoice.getValue());
                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Data berhasil");
                alert.setHeaderText("Data telah berhasil dimasukan");
                alert.showAndWait();
            } catch (SQLException e) {
                System.out.println("Error SimpanWali: " + e);
            }
        }
    }
}
