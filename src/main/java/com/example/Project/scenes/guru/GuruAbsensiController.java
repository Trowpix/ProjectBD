package com.example.Project.scenes.guru;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.SiswaAbsensi;
import com.example.Project.dtos.User;
import com.example.Project.scenes.walikelas.WaliKelasViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Arrays;
import java.util.List;

public class GuruAbsensiController {

    @FXML private ComboBox<String> kelasComboBox;
    @FXML private ComboBox<String> mapelComboBox;
    @FXML private ComboBox<String> hariComboBox;
    @FXML private Label tanggalLabel;
    @FXML private TableView<SiswaAbsensi> absensiTableView;
    @FXML private TableColumn<SiswaAbsensi, String> nisColumn;
    @FXML private TableColumn<SiswaAbsensi, String> namaColumn;
    @FXML private TableColumn<SiswaAbsensi, CheckBox> hadirColumn;
    @FXML private TableColumn<SiswaAbsensi, CheckBox> izinColumn;
    @FXML private TableColumn<SiswaAbsensi, CheckBox> alphaColumn;

    private User user;
    private final ObservableList<SiswaAbsensi> siswaList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
        populateKelasComboBox();
    }

    @FXML
    void initialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        tanggalLabel.setText("Tanggal Absensi Hari Ini: " + LocalDate.now().format(formatter));

        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        hadirColumn.setCellValueFactory(new PropertyValueFactory<>("hadir"));
        izinColumn.setCellValueFactory(new PropertyValueFactory<>("izin"));
        alphaColumn.setCellValueFactory(new PropertyValueFactory<>("alpha"));

        absensiTableView.setItems(siswaList);

        kelasComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> populateMapelComboBox(n));
        mapelComboBox.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> populateHariComboBox(n));
    }

    private void populateKelasComboBox() {
        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT k.nama_kelas FROM kelas k JOIN jadwal_kelas jk ON k.id_kelas = jk.id_kelas WHERE jk.nip_guru = ? ORDER BY k.nama_kelas";
        try (Connection c = MainDataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user.id);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("nama_kelas"));
            kelasComboBox.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void populateMapelComboBox(String selectedKelas) {
        clearSelections(mapelComboBox, hariComboBox);
        if (selectedKelas == null) return;

        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT mp.nama_mata_pelajaran FROM mata_pelajaran mp JOIN jadwal_kelas jk ON mp.id_mata_pelajaran = jk.id_mata_pelajaran WHERE jk.nip_guru = ? AND jk.id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)";
        try (Connection c = MainDataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user.id);
            p.setString(2, selectedKelas);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("nama_mata_pelajaran"));
            mapelComboBox.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void populateHariComboBox(String selectedMapel) {
        clearSelections(hariComboBox);
        if (selectedMapel == null || kelasComboBox.getValue() == null) return;

        ObservableList<String> list = FXCollections.observableArrayList();
        String sql = "SELECT DISTINCT hari_jadwal_kelas FROM jadwal_kelas WHERE nip_guru = ? AND id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?)";
        try (Connection c = MainDataSource.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user.id);
            p.setString(2, kelasComboBox.getValue());
            p.setString(3, selectedMapel);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(rs.getString("hari_jadwal_kelas"));
            hariComboBox.setItems(list);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void clearSelections(ComboBox<?>... comboBoxes) {
        for (ComboBox<?> cb : comboBoxes) {
            cb.getItems().clear();
            cb.setValue(null);
        }
        siswaList.clear();
    }

    @FXML
    void onTerapkanClicked() {
        String selectedKelas = kelasComboBox.getValue();
        String selectedMapel = mapelComboBox.getValue();
        String selectedHari = hariComboBox.getValue();

        if (selectedKelas == null || selectedMapel == null || selectedHari == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih Kelas, Mata Pelajaran, dan Hari terlebih dahulu.");
            return;
        }

        siswaList.clear();

        String sql = "SELECT s.nomor_induk_siswa, s.nama_siswa, a.status_absensi " +
                "FROM siswa s " +
                "LEFT JOIN absensi a ON s.nomor_induk_siswa = a.nomor_induk_siswa " +
                "AND a.tanggal_absensi = ? " +
                "AND a.id_jadwal_kelas = (" +
                "    SELECT id_jadwal_kelas FROM jadwal_kelas " +
                "    WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) " +
                "    AND id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?) " +
                "    AND hari_jadwal_kelas = ? AND nip_guru = ? ORDER BY jam_jadwal_kelas ASC LIMIT 1" +
                ") " +
                "WHERE s.id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) " +
                "ORDER BY s.nama_siswa";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, selectedKelas);
            pstmt.setString(3, selectedMapel);
            pstmt.setString(4, selectedHari);
            pstmt.setString(5, user.id);
            pstmt.setString(6, selectedKelas);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                SiswaAbsensi siswa = new SiswaAbsensi(rs.getString("nomor_induk_siswa"), rs.getString("nama_siswa"));
                String status = rs.getString("status_absensi");

                if (status != null) {
                    switch (status) {
                        case "Hadir" -> siswa.getHadir().setSelected(true);
                        case "Izin"  -> siswa.getIzin().setSelected(true);
                        case "Alpha" -> siswa.getAlpha().setSelected(true);
                    }
                }
                siswaList.add(siswa);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onSimpanAbsensiClicker() {
        String selectedHari = hariComboBox.getValue();
        if (kelasComboBox.getValue() == null || mapelComboBox.getValue() == null || selectedHari == null) {
            showAlert(Alert.AlertType.WARNING, "Data Tidak Lengkap", "Harap isi semua pilihan sebelum menyimpan.");
            return;
        }

        String namaHariIni = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
        if (!selectedHari.equalsIgnoreCase(namaHariIni)) {
            showAlert(Alert.AlertType.ERROR, "Hari Tidak Sesuai", "Anda hanya bisa mengambil absensi untuk hari ini (" + namaHariIni + ").");
            return;
        }

        Connection conn = null;
        try {
            conn = MainDataSource.getConnection();

            PreparedStatement jadwalStmt = conn.prepareStatement("SELECT id_jadwal_kelas FROM jadwal_kelas WHERE nip_guru = ? AND hari_jadwal_kelas = ? AND id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) AND id_mata_pelajaran = (SELECT id_mata_pelajaran FROM mata_pelajaran WHERE nama_mata_pelajaran = ?) ORDER BY jam_jadwal_kelas ASC LIMIT 1");
            jadwalStmt.setString(1, user.id);
            jadwalStmt.setString(2, selectedHari);
            jadwalStmt.setString(3, kelasComboBox.getValue());
            jadwalStmt.setString(4, mapelComboBox.getValue());
            ResultSet rsJadwal = jadwalStmt.executeQuery();

            if (!rsJadwal.next()) {
                showAlert(Alert.AlertType.ERROR, "Jadwal Tidak Ditemukan", "Tidak ada jadwal yang cocok untuk kriteria yang dipilih.");
                return;
            }
            int idJadwalKelas = rsJadwal.getInt("id_jadwal_kelas");
            conn.setAutoCommit(false);

            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM absensi WHERE id_jadwal_kelas = ? AND tanggal_absensi = ?");
            deleteStmt.setInt(1, idJadwalKelas);
            deleteStmt.setDate(2, Date.valueOf(LocalDate.now()));
            deleteStmt.executeUpdate();

            String insertSql = "INSERT INTO absensi (nomor_induk_siswa, id_jadwal_kelas, tanggal_absensi, status_absensi) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            for (SiswaAbsensi siswa : siswaList) {
                String status = siswa.getStatus();
                if (status != null) {
                    insertStmt.setString(1, siswa.getNis());
                    insertStmt.setInt(2, idJadwalKelas);
                    insertStmt.setDate(3, Date.valueOf(LocalDate.now()));
                    insertStmt.setString(4, status);
                    insertStmt.addBatch();
                }
            }
            insertStmt.executeBatch();
            conn.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data absensi berhasil disimpan.");

        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan absensi: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }


    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            if ("Wali Kelas".equals(user.role)) {
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