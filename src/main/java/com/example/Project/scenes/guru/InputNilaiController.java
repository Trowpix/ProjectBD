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

    @FXML private Label namaSiswaLabel;
    @FXML private TextField utsTextField;
    @FXML private TextField uasTextField;
    @FXML private ComboBox<String> nisComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private ComboBox<String> mapelComboBox;
    @FXML private ComboBox<String> kelasComboBox;
    @FXML private Label guruNameLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        initializeComboBoxes();
        guruNameLabel.setText("Guru: " + user.username);
    }

    @FXML
    void initialize() {
        semesterComboBox.getItems().addAll("1", "2");
        semesterComboBox.setValue("1");

        mapelComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> populateKelasComboBox(newValue));
        kelasComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> populateNisComboBox(newValue));
        nisComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> onNisPicked());
        semesterComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> onNisPicked());
    }

    private void initializeComboBoxes() {
        if (user == null || user.id == null) return;
        ObservableList<String> mapelList = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT mp.nama_mata_pelajaran FROM mata_pelajaran mp JOIN jadwal_kelas jk ON mp.id_mata_pelajaran = jk.id_mata_pelajaran WHERE jk.nip_guru = ?";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mapelList.add(rs.getString("nama_mata_pelajaran"));
            }
            mapelComboBox.setItems(mapelList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateKelasComboBox(String selectedMapel) {
        if (selectedMapel == null) return;
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT k.nama_kelas FROM kelas k JOIN jadwal_kelas jk ON k.id_kelas = jk.id_kelas WHERE jk.nip_guru = ? AND jk.id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?)";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.id);
            pstmt.setString(2, selectedMapel);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            kelasComboBox.setItems(kelasList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void populateNisComboBox(String selectedKelas) {
        if (selectedKelas == null) return;
        ObservableList<String> nisList = FXCollections.observableArrayList();
        String sql = "SELECT nomor_induk_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, selectedKelas);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                nisList.add(rs.getString("nomor_induk_siswa"));
            }
            nisComboBox.setItems(nisList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onNisPicked() {
        String selectedNis = nisComboBox.getValue();
        String selectedMapel = mapelComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();

        if (selectedNis == null || selectedMapel == null || selectedSemester == null) {
            utsTextField.clear();
            uasTextField.clear();
            namaSiswaLabel.setText("-");
            return;
        }

        String sql = "SELECT nama_siswa, uts, uas FROM siswa s LEFT JOIN nilai_ujian n ON s.nomor_induk_siswa = n.nomor_induk_siswa AND n.id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?) AND n.semester = ? WHERE s.nomor_induk_siswa = ?";

        try (Connection data = MainDataSource.getConnection();
             PreparedStatement stmt = data.prepareStatement(sql)) {

            stmt.setString(1, selectedMapel);
            stmt.setString(2, selectedSemester);
            stmt.setString(3, selectedNis);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                namaSiswaLabel.setText(rs.getString("nama_siswa"));
                int uts = rs.getInt("uts");
                int uas = rs.getInt("uas");

                if (rs.wasNull()) {
                    utsTextField.clear();
                    uasTextField.clear();
                } else {
                    utsTextField.setText(String.valueOf(uts));
                    uasTextField.setText(String.valueOf(uas));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSimpanNilaiClicked() {
        if (!isInputValid()) {
            return;
        }

        String sql = "INSERT INTO nilai_ujian (nomor_induk_siswa, nip_guru, id_mata_pelajaran, semester, uts, uas) " +
                "VALUES (?, ?, (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?), ?, ?, ?) " +
                "ON CONFLICT (nomor_induk_siswa, id_mata_pelajaran, semester) " +
                "DO UPDATE SET uts = EXCLUDED.uts, uas = EXCLUDED.uas, nip_guru = EXCLUDED.nip_guru";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nisComboBox.getValue());
            pstmt.setString(2, user.id);
            pstmt.setString(3, mapelComboBox.getValue());
            pstmt.setString(4, semesterComboBox.getValue());
            pstmt.setInt(5, Integer.parseInt(utsTextField.getText()));
            pstmt.setInt(6, Integer.parseInt(uasTextField.getText()));
            pstmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Berhasil", "Nilai telah berhasil disimpan atau diperbarui.");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal menyimpan nilai.");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Salah", "Nilai UTS dan UAS harus berupa angka.");
        }
    }

    // Metode lain tidak berubah
    @FXML
    void onTampilkanSiswaClicked() {
        if (kelasComboBox.getValue() != null) {
            try {
                MainMenu app = MainMenu.getApplicationInstance();
                app.getPrimaryStage().setTitle("Tampilan Siswa");
                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-inputTampilkanSiswa.fxml"));
                Parent root = loader.load();
                InputTampilkanSiswa inputTampilkanSiswa = loader.getController();
                inputTampilkanSiswa.setUser(user);
                inputTampilkanSiswa.setKelas(kelasComboBox.getValue());
                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas terlebih dahulu untuk menampilkan siswa.");
        }
    }
    private boolean isInputValid() {
        if (utsTextField.getText().isEmpty() || uasTextField.getText().isEmpty() || nisComboBox.getValue() == null || semesterComboBox.getValue() == null || kelasComboBox.getValue() == null || mapelComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap isi semua field sebelum menyimpan.");
            return false;
        }
        return true;
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
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}