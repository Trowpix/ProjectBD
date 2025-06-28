package com.example.Project.scenes.walikelas;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableViewGrade;
import com.example.Project.dtos.User;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

        tahunAjaranComboBox.getItems().addAll("2024/2025", "2025/2026", "2026/2027");
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
                double rataRata = (uts + uas) / 2.0;
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
        if (raporTable.getItems().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Data Kosong", "Generate rapor terlebih dahulu sebelum mencetak.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Simpan Rapor PDF");
        String initialFileName = String.format("Rapor_%s_%s.pdf", namaSiswaLabel.getText().replace(" ", "_"), kelasLabel.getText().replace("Kelas: ", "").trim());
        fileChooser.setInitialFileName(initialFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(raporTable.getScene().getWindow());

        if (file != null) {
            try {
                createPdf(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "File PDF berhasil disimpan di:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi error saat membuat file PDF.");
                e.printStackTrace();
            }
        }
    }

    private void createPdf(String dest) throws IOException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("RAPOR SISWA").setTextAlignment(TextAlignment.CENTER).setFontSize(20).setBold());
        document.add(new Paragraph("TAHUN AJARAN " + tahunAjaranComboBox.getValue()).setTextAlignment(TextAlignment.CENTER).setFontSize(14));
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Nama Siswa\t: " + namaSiswaLabel.getText()));
        document.add(new Paragraph("Nomor Induk Siswa\t: " + siswaComboBox.getValue()));
        document.add(new Paragraph(kelasLabel.getText()));
        document.add(new Paragraph(waliLabel.getText()));
        document.add(new Paragraph("Semester\t: " + semesterComboBox.getValue()));
        document.add(new Paragraph("\n\n"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 5, 2, 2, 2}));
        table.setWidth(UnitValue.createPercentValue(100));

        table.addHeaderCell(new Cell().add(new Paragraph("No.").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Mata Pelajaran").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nilai UTS").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Nilai UAS").setBold()));
        table.addHeaderCell(new Cell().add(new Paragraph("Rata-rata").setBold()));

        int counter = 1;
        for (TableViewGrade grade : raporTable.getItems()) {
            table.addCell(String.valueOf(counter++));
            table.addCell(grade.getMapelName());
            table.addCell(String.valueOf(grade.getUtsValue()));
            table.addCell(String.valueOf(grade.getUasValue()));
            table.addCell(String.format("%.2f", grade.getRataRataValue()));
        }
        document.add(table);

        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Nilai Akhir Rata-rata: " + nilaiAkhirLabel.getText()).setFontSize(14).setBold().setTextAlignment(TextAlignment.RIGHT));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
        String formattedDate = LocalDate.now().format(dtf);
        document.add(new Paragraph("\n\nSurabaya, " + formattedDate).setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("\n\n\n\n"));
        document.add(new Paragraph(user.username).setTextAlignment(TextAlignment.RIGHT));

        document.close();
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