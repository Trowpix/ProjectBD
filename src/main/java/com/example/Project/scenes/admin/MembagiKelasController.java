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

    private User user;

    public void setUser(User user) {
        this.user = user;
        initializeChoiceBox();
        updateUnassignedStudents();
    }

    @FXML
    void initialize() {
        assignedStudentsList.setItems(FXCollections.observableArrayList());
    }

    private void updateUnassignedStudents() {
        ObservableList<String> unassigned = FXCollections.observableArrayList();
        String sql = "SELECT nomor_induk_siswa, nama_siswa FROM siswa WHERE id_kelas IS NULL ORDER BY nama_siswa";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                unassigned.add("(" + rs.getString("nomor_induk_siswa") + ") - " + rs.getString("nama_siswa"));
            }
            unassignedStudentsList.setItems(unassigned);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeChoiceBox() {
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT nama_kelas FROM kelas ORDER BY nama_kelas");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            kelasChoiceBox.setItems(kelasList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onTampilkanDataClicked() {
        String selectedKelas = kelasChoiceBox.getValue();
        if (selectedKelas == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas terlebih dahulu.");
            return;
        }

        onResetClicked();
        kelasChoiceBox.setValue(selectedKelas);
        kelasLabel.setText(selectedKelas);

        try (Connection conn = MainDataSource.getConnection()) {
            // Mengisi daftar siswa yang sudah ada di kelas
            ObservableList<String> assigned = FXCollections.observableArrayList();
            PreparedStatement stmt = conn.prepareStatement("SELECT nomor_induk_siswa, nama_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) ORDER BY nama_siswa");
            stmt.setString(1, selectedKelas);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                assigned.add("(" + rs.getString("nomor_induk_siswa") + ") - " + rs.getString("nama_siswa"));
            }
            assignedStudentsList.setItems(assigned);
            jumlahSiswaLabel.setText(String.valueOf(assigned.size()));

            String currentWaliNama = null;
            stmt = conn.prepareStatement("SELECT g.nama_guru FROM guru g JOIN wali_kelas wk ON g.nip = wk.nip_guru WHERE wk.id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)");
            stmt.setString(1, selectedKelas);
            rs = stmt.executeQuery();
            if (rs.next()) {
                currentWaliNama = rs.getString("nama_guru");
                waliKelasLabel.setText(currentWaliNama);
            } else {
                waliKelasLabel.setText("Belum Dipilih");
            }

            ObservableList<String> availableGuru = FXCollections.observableArrayList();
            stmt = conn.prepareStatement("SELECT nama_guru FROM guru WHERE nip NOT IN (SELECT nip_guru FROM wali_kelas)");
            rs = stmt.executeQuery();
            while(rs.next()){
                availableGuru.add(rs.getString("nama_guru"));
            }

            if(currentWaliNama != null && !availableGuru.contains(currentWaliNama)){
                availableGuru.add(0, currentWaliNama);
            }

            waliKelasChoice.setItems(availableGuru);
            waliKelasChoice.setValue(currentWaliNama);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSimpanWaliKelasClicked() {
        String selectedWaliNama = waliKelasChoice.getValue();
        String selectedKelasNama = kelasChoiceBox.getValue();

        if (selectedWaliNama == null || selectedKelasNama == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas dan wali kelas terlebih dahulu.");
            return;
        }

        String sql = "INSERT INTO wali_kelas (id_kelas, nip_guru) " +
                "VALUES ((SELECT id_kelas FROM kelas WHERE nama_kelas = ?), (SELECT nip FROM guru WHERE nama_guru = ?)) " +
                "ON CONFLICT (id_kelas) DO UPDATE SET nip_guru = EXCLUDED.nip_guru";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, selectedKelasNama);
            pstmt.setString(2, selectedWaliNama);

            pstmt.executeUpdate();

            waliKelasLabel.setText(selectedWaliNama); // Update label di UI
            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Data wali kelas telah berhasil disimpan.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat menyimpan data wali kelas.");
        }
    }

    @FXML
    void onSimpanClicked() {
        if (kelasChoiceBox.getValue() == null){
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas terlebih dahulu sebelum menyimpan.");
            return;
        }

        try (Connection data = MainDataSource.getConnection()) {
            data.setAutoCommit(false);

            PreparedStatement updateAssignedStmt = data.prepareStatement("UPDATE siswa SET id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) WHERE nomor_induk_siswa = ?");
            for (String siswaInfo : assignedStudentsList.getItems()) {
                updateAssignedStmt.setString(1, kelasChoiceBox.getValue());
                updateAssignedStmt.setString(2, getNis(siswaInfo));
                updateAssignedStmt.addBatch();
            }
            updateAssignedStmt.executeBatch();

            PreparedStatement updateUnassignedStmt = data.prepareStatement("UPDATE siswa SET id_kelas = NULL WHERE nomor_induk_siswa = ?");
            for (String siswaInfo : unassignedStudentsList.getItems()) {
                updateUnassignedStmt.setString(1, getNis(siswaInfo));
                updateUnassignedStmt.addBatch();
            }
            updateUnassignedStmt.executeBatch();

            data.commit();

            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Pembagian kelas siswa telah berhasil disimpan.");
        } catch (SQLException e) {
            System.out.println("Error SimpanClicked: " + e);
            showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat menyimpan pembagian kelas.");
        }
    }

    @FXML
    void onResetClicked() {
        updateUnassignedStudents();
        kelasChoiceBox.setValue(null);
        if (waliKelasChoice.getItems() != null) {
            waliKelasChoice.getItems().clear();
        }
        waliKelasChoice.setValue(null);
        if (assignedStudentsList.getItems() != null) {
            assignedStudentsList.getItems().clear();
        }
        kelasLabel.setText("Nama Kelas");
        jumlahSiswaLabel.setText("0");
        waliKelasLabel.setText("Belum Dipilih");
    }

    @FXML
    void onTambahkanClicked() {
        String selectedSiswa = unassignedStudentsList.getSelectionModel().getSelectedItem();
        if (selectedSiswa != null) {
            assignedStudentsList.getItems().add(selectedSiswa);
            unassignedStudentsList.getItems().remove(selectedSiswa);
            jumlahSiswaLabel.setText(String.valueOf(assignedStudentsList.getItems().size()));
        }
    }

    @FXML
    void onKembalikanClicked() {
        String selectedSiswa = assignedStudentsList.getSelectionModel().getSelectedItem();
        if (selectedSiswa != null){
            unassignedStudentsList.getItems().add(selectedSiswa);
            assignedStudentsList.getItems().remove(selectedSiswa);
            jumlahSiswaLabel.setText(String.valueOf(assignedStudentsList.getItems().size()));
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");

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

    private String getNis(String s){
        if (s == null || !s.contains("(") || !s.contains(")")) {
            return "";
        }
        return s.substring(s.indexOf('(') + 1, s.indexOf(')'));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}