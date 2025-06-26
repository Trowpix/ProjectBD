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
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GuruAbsensiController {

    @FXML
    private ComboBox<String> kelasComboBox;
    @FXML
    private ComboBox<String> mapelComboBox;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<Time> jamMulaiComboBox;
    @FXML
    private ListView<String> belumAbsenListView;
    @FXML
    private ListView<String> hadirListView;
    @FXML
    private ListView<String> izinListView;
    @FXML
    private ListView<String> alphaListView;

    @FXML
    private Button hadirButton;
    @FXML
    private Button izinButton;
    @FXML
    private Button alphaButton;
    @FXML
    private Button belumAbsenButton;

    private User user = new User();
    private List<ListView<String>> allListViews;


    public void setUser(User user) {
        this.user = user;
        initializeComboBoxes();
    }

    @FXML
    void initialize(){
        datePicker.setValue(LocalDate.now());
        allListViews = Arrays.asList(belumAbsenListView, hadirListView, izinListView, alphaListView);
        setupMutualExclusionSelection();
    }

    private void initializeComboBoxes() {
        ObservableList<String> kelasList = FXCollections.observableArrayList();
        ObservableList<String> mapelList = FXCollections.observableArrayList();
        ObservableList<Time> jamList = FXCollections.observableArrayList();

        try (Connection data = MainDataSource.getConnection()) {
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT k.nama_kelas FROM kelas k JOIN jadwal_kelas jk ON k.id_kelas = jk.id_kelas WHERE jk.nip_guru = ? ORDER BY k.nama_kelas");
            stmt.setString(1, user.id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                kelasList.add(rs.getString("nama_kelas"));
            }
            kelasComboBox.setItems(kelasList);

            stmt = data.prepareStatement("SELECT DISTINCT mp.nama_mata_pelajaran FROM mata_pelajaran mp JOIN jadwal_kelas jk ON mp.id_mata_pelajaran = jk.id_mata_pelajaran WHERE jk.nip_guru = ? ORDER BY mp.nama_mata_pelajaran");
            stmt.setString(1, user.id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                mapelList.add(rs.getString("nama_mata_pelajaran"));
            }
            mapelComboBox.setItems(mapelList);

            stmt = data.prepareStatement("SELECT DISTINCT jam_jadwal_kelas FROM jadwal_kelas ORDER BY jam_jadwal_kelas");
            rs = stmt.executeQuery();
            while(rs.next()){
                jamList.add(rs.getTime("jam_jadwal_kelas"));
            }
            jamMulaiComboBox.setItems(jamList);

        } catch (SQLException e) {
            System.err.println("Error initializing ComboBoxes: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data awal: " + e.getMessage());
        }
    }

    private void setupMutualExclusionSelection() {
        for (ListView<String> currentList : allListViews) {
            currentList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    for (ListView<String> otherList : allListViews) {
                        if (otherList != currentList) {
                            otherList.getSelectionModel().clearSelection();
                        }
                    }
                }
            });
        }
    }

    private void moveSelectedItemTo(ListView<String> targetList) {
        String selectedStudent = null;
        ListView<String> sourceList = null;

        for (ListView<String> currentList : allListViews) {
            String selection = currentList.getSelectionModel().getSelectedItem();
            if (selection != null) {
                selectedStudent = selection;
                sourceList = currentList;
                break;
            }
        }

        if (selectedStudent != null && sourceList != targetList) {
            targetList.getItems().add(selectedStudent);
            sourceList.getItems().remove(selectedStudent);
        }
    }

    @FXML
    void onHadirClicked() {
        moveSelectedItemTo(hadirListView);
    }

    @FXML
    void onIzinClicked() {
        moveSelectedItemTo(izinListView);
    }

    @FXML
    void onAlphaClicked() {
        moveSelectedItemTo(alphaListView);
    }

    @FXML
    void onBelumAbsenClicked() {
        moveSelectedItemTo(belumAbsenListView);
    }

    @FXML
    void onTerapkanClicked() {
        if (kelasComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih kelas terlebih dahulu.");
            return;
        }
        onResetClicked();

        ObservableList<String> students = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()) {
            PreparedStatement stmt = data.prepareStatement(
                    "SELECT nomor_induk_siswa, nama_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?) ORDER BY nama_siswa"
            );
            stmt.setString(1, kelasComboBox.getValue());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                students.add(String.format("(%s) - %s", rs.getString("nomor_induk_siswa"), rs.getString("nama_siswa")));
            }
            belumAbsenListView.setItems(students);

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal memuat data siswa: " + e.getMessage());
        }
    }

    @FXML
    void onSimpanAbsensiClicker() {
        if (kelasComboBox.getValue() == null || mapelComboBox.getValue() == null || datePicker.getValue() == null || jamMulaiComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Data Tidak Lengkap", "Harap isi semua pilihan (Kelas, Mapel, Tanggal, Jam) sebelum menyimpan.");
            return;
        }

        try (Connection data = MainDataSource.getConnection()) {
            DayOfWeek day = datePicker.getValue().getDayOfWeek();
            String namaHari = day.getDisplayName(TextStyle.FULL, new Locale("id", "ID"));

            PreparedStatement jadwalStmt = data.prepareStatement(
                    "SELECT id_jadwal_kelas FROM jadwal_kelas jk " +
                            "JOIN kelas k ON jk.id_kelas = k.id_kelas " +
                            "JOIN mata_pelajaran mp ON jk.id_mata_pelajaran = mp.id_mata_pelajaran " +
                            "WHERE k.nama_kelas = ? AND mp.nama_mata_pelajaran = ? AND jk.hari_jadwal_kelas = ? AND jk.jam_jadwal_kelas = ? AND jk.nip_guru = ?");
            jadwalStmt.setString(1, kelasComboBox.getValue());
            jadwalStmt.setString(2, mapelComboBox.getValue());
            jadwalStmt.setString(3, namaHari);
            jadwalStmt.setTime(4, jamMulaiComboBox.getValue());
            jadwalStmt.setString(5, user.id);

            ResultSet rsJadwal = jadwalStmt.executeQuery();

            if (!rsJadwal.next()) {
                showAlert(Alert.AlertType.ERROR, "Jadwal Tidak Ditemukan", "Tidak ada jadwal yang cocok untuk kriteria yang dipilih pada hari " + namaHari + ".");
                return;
            }
            int idJadwalKelas = rsJadwal.getInt("id_jadwal_kelas");

            data.setAutoCommit(false);

            PreparedStatement deleteStmt = data.prepareStatement("DELETE FROM absensi WHERE id_jadwal_kelas = ? AND tanggal_absensi = ?");
            deleteStmt.setInt(1, idJadwalKelas);
            deleteStmt.setDate(2, Date.valueOf(datePicker.getValue()));
            deleteStmt.executeUpdate();

            // Save the marked students
            saveListToDb(data, hadirListView.getItems(), "Hadir", idJadwalKelas);
            saveListToDb(data, izinListView.getItems(), "Izin", idJadwalKelas);
            saveListToDb(data, alphaListView.getItems(), "Alpha", idJadwalKelas);

            // **NEW**: Save students left in "Belum Absen" list with NULL status
            saveListToDb(data, belumAbsenListView.getItems(), null, idJadwalKelas);

            data.commit();
            showAlert(Alert.AlertType.INFORMATION, "Sukses", "Data absensi berhasil disimpan.");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Gagal menyimpan absensi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveListToDb(Connection connection, List<String> studentList, String status, int idJadwalKelas) throws SQLException {
        String sql = "INSERT INTO absensi (nomor_induk_siswa, id_jadwal_kelas, id_rapor, tanggal_absensi, status_absensi) VALUES (?, ?, NULL, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String studentInfo : studentList) {
                stmt.setString(1, getNis(studentInfo));
                stmt.setInt(2, idJadwalKelas);
                // id_rapor is parameter 3
                stmt.setDate(3, Date.valueOf(datePicker.getValue()));

                // **UPDATED**: Handle NULL for status_absensi
                if (status != null) {
                    stmt.setString(4, status);
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }

                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    @FXML
    void onResetClicked() {
        for (ListView<String> list : allListViews) {
            if(list.getItems() != null) {
                list.getItems().clear();
            }
        }
    }

    private String getNis(String s) {
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

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            if ("Wali Kelas".equals(user.role)) {
                app.getPrimaryStage().setTitle("Wali Kelas View");

                FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-walikelasView.fxml"));
                Parent root = loader.load();

                WaliKelasViewController waliKelasController = loader.getController();
                waliKelasController.setUser(user);

                Scene scene = new Scene(root);
                app.getPrimaryStage().setScene(scene);
            } else {
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
}