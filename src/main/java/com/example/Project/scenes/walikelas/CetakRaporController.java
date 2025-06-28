package com.example.Project.scenes.walikelas;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableViewGrade;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CetakRaporController {
    @FXML private Label nilaiAkhirLabel;
    @FXML private Label kelasLabel;
    @FXML private Label waliLabel;
    @FXML private ComboBox<String> siswaComboBox;
    @FXML private ComboBox<String> tahunAjaranComboBox;
    @FXML private ComboBox<String> semesterComboBox;
    @FXML private Label namaSiswaLabel;
    @FXML private TableView<TableViewGrade> raporTable;
    @FXML private TableColumn<TableViewGrade, String> mapelColumn;
    @FXML private TableColumn<TableViewGrade, Double> utsColumn;
    @FXML private TableColumn<TableViewGrade, Double> uasColumn;
    @FXML private TableColumn<TableViewGrade, Double> rataRataColumn;

    private User user;

    public void setUser(User user) {
        this.user = user;
        initializeData();
    }

    @FXML
    void initialize() {
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapelName"));
        utsColumn.setCellValueFactory(new PropertyValueFactory<>("utsValue"));
        uasColumn.setCellValueFactory(new PropertyValueFactory<>("uasValue"));
        rataRataColumn.setCellValueFactory(new PropertyValueFactory<>("rataRataValue"));
    }

    private void initializeData() {
        if (user == null) return;

        waliLabel.setText("Wali Kelas: " + user.username);

        ObservableList<String> nisList = FXCollections.observableArrayList();
        semesterComboBox.getItems().addAll("1", "2");
        semesterComboBox.setValue("1");

        // Contoh data untuk tahun ajaran, karena tidak ada sumber data yang pasti
        tahunAjaranComboBox.getItems().addAll("2024/2025", "2025/2026");
        tahunAjaranComboBox.setValue("2024/2025");

        try (Connection conn = MainDataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT nama_kelas FROM kelas WHERE id_kelas = (SELECT id_kelas FROM wali_kelas WHERE nip_guru = ?)");
            stmt.setString(1, user.id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String namaKelas = rs.getString("nama_kelas");
                kelasLabel.setText("Kelas: " + namaKelas);

                stmt = conn.prepareStatement("SELECT nomor_induk_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = ?)");
                stmt.setString(1, namaKelas);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    nisList.add(rs.getString("nomor_induk_siswa"));
                }
                siswaComboBox.setItems(nisList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onGenerateClicked() {
        String selectedNis = siswaComboBox.getValue();
        String selectedSemester = semesterComboBox.getValue();

        if (selectedNis == null || selectedSemester == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih Siswa (NIS) dan Semester terlebih dahulu.");
            return;
        }

        ObservableList<TableViewGrade> grades = FXCollections.observableArrayList();
        String sql = "SELECT mp.nama_mata_pelajaran, n.uts, n.uas FROM nilai_ujian n " +
                "JOIN mata_pelajaran mp ON n.id_mata_pelajaran = mp.id_mata_pelajaran " +
                "WHERE n.nomor_induk_siswa = ? AND n.semester = ? " +
                "ORDER BY mp.nama_mata_pelajaran";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, selectedNis);
            pstmt.setString(2, selectedSemester);
            ResultSet rs = pstmt.executeQuery();

            double totalNilaiRataRata = 0;
            int jumlahMapel = 0;

            while (rs.next()) {
                String mapel = rs.getString("nama_mata_pelajaran");
                double uts = rs.getDouble("uts");
                double uas = rs.getDouble("uas");
                double rataRata = (uts + uas) / 2;
                grades.add(new TableViewGrade(mapel, uts, uas, rataRata));
                totalNilaiRataRata += rataRata;
                jumlahMapel++;
            }
            raporTable.setItems(grades);

            if (jumlahMapel > 0) {
                double nilaiAkhir = totalNilaiRataRata / jumlahMapel;
                nilaiAkhirLabel.setText(String.format("%.2f", nilaiAkhir));
            } else {
                nilaiAkhirLabel.setText("0");
            }

            PreparedStatement namaStmt = conn.prepareStatement("SELECT nama_siswa FROM siswa WHERE nomor_induk_siswa = ?");
            namaStmt.setString(1, selectedNis);
            ResultSet namaRs = namaStmt.executeQuery();
            if(namaRs.next()){
                namaSiswaLabel.setText(namaRs.getString("nama_siswa"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Gagal", "Gagal mengambil data nilai dari database.");
        }
    }

    @FXML
    void onCetakPdfClicked() {
        showAlert(Alert.AlertType.INFORMATION, "Fitur Dalam Pengembangan", "Fungsionalitas untuk mencetak ke PDF memerlukan library tambahan dan saat ini sedang dalam pengembangan.");
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Wali Kelas View");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-walikelasView.fxml"));
            Parent root = loader.load();
            WaliKelasViewController controller = loader.getController();
            controller.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
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