package com.example.Project.scenes.guru;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.Ekstrakurikuler;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class GuruManajemenEkskulController {

    @FXML private TableView<Ekstrakurikuler> tableViewEskul;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomNama;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomJadwal;
    @FXML private TableColumn<Ekstrakurikuler, Void> kolomAksi;
    @FXML private Label formLabel;
    @FXML private TextField fieldNama;
    @FXML private DatePicker datePickerJadwal;
    @FXML private Button tombolSimpan;
    @FXML private Button tombolBatal;

    private User user;
    private final ObservableList<Ekstrakurikuler> eskulList = FXCollections.observableArrayList();
    private Ekstrakurikuler eskulTerpilih = null;

    public void setUser(User user) {
        this.user = user;
        loadData();
    }

    @FXML
    public void initialize() {
        kolomNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kolomJadwal.setCellValueFactory(new PropertyValueFactory<>("jadwal"));
        addAksiButtonsToTable();
        resetForm();
    }

    private void loadData() {
        if (user == null || user.id == null) return;
        eskulList.clear();
        String sql = "SELECT id_ekskul, nama_ekskul, jadwal FROM ekstrakurikuler WHERE nip_pembina = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.id);
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            while (rs.next()) {
                Date dbDate = rs.getDate("jadwal");
                String formattedDate = (dbDate != null) ? dbDate.toLocalDate().format(formatter) : "Belum diatur";

                eskulList.add(new Ekstrakurikuler(
                        rs.getString("id_ekskul"),
                        rs.getString("nama_ekskul"),
                        formattedDate,
                        user.username
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableViewEskul.setItems(eskulList);
    }

    private void addAksiButtonsToTable() {
        Callback<TableColumn<Ekstrakurikuler, Void>, TableCell<Ekstrakurikuler, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Hapus");
                private final HBox pane = new HBox(5, editBtn, deleteBtn);

                {
                    editBtn.setOnAction(event -> {
                        eskulTerpilih = getTableView().getItems().get(getIndex());
                        siapkanFormEdit();
                    });
                    deleteBtn.setOnAction(event -> {
                        Ekstrakurikuler eskul = getTableView().getItems().get(getIndex());
                        hapusEskul(eskul);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : pane);
                }
            };
        };
        kolomAksi.setCellFactory(cellFactory);
    }

    @FXML
    void onSimpanClicked() {
        if (fieldNama.getText().isEmpty() || datePickerJadwal.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Peringatan", "Nama Ekstrakurikuler dan Jadwal harus diisi.");
            return;
        }

        if (eskulTerpilih == null) {
            tambahEskul();
        } else {
            updateEskul();
        }
    }

    private void tambahEskul() {
        String sql = "INSERT INTO ekstrakurikuler (nama_ekskul, jadwal, nip_pembina) VALUES (?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fieldNama.getText());
            pstmt.setDate(2, Date.valueOf(datePickerJadwal.getValue()));
            pstmt.setString(3, user.id);
            pstmt.executeUpdate();
            loadData();
            resetForm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEskul() {
        String sql = "UPDATE ekstrakurikuler SET nama_ekskul = ?, jadwal = ? WHERE id_ekskul = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fieldNama.getText());
            pstmt.setDate(2, Date.valueOf(datePickerJadwal.getValue()));
            pstmt.setInt(3, Integer.parseInt(eskulTerpilih.getId()));
            pstmt.executeUpdate();
            loadData();
            resetForm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void hapusEskul(Ekstrakurikuler eskul) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Anda yakin ingin menghapus eskul: " + eskul.getNama() + "?");
        alert.setContentText("Tindakan ini juga akan menghapus data pendaftaran siswa pada eskul ini.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String deletePendaftaranSql = "DELETE FROM siswa_ekstrakurikuler WHERE id_ekskul = ?";
            String deleteEskulSql = "DELETE FROM ekstrakurikuler WHERE id_ekskul = ?";

            Connection conn = null;
            try {
                conn = MainDataSource.getConnection();
                conn.setAutoCommit(false);

                try(PreparedStatement pstmt1 = conn.prepareStatement(deletePendaftaranSql)){
                    pstmt1.setInt(1, Integer.parseInt(eskul.getId()));
                    pstmt1.executeUpdate();
                }

                try(PreparedStatement pstmt2 = conn.prepareStatement(deleteEskulSql)){
                    pstmt2.setInt(1, Integer.parseInt(eskul.getId()));
                    pstmt2.executeUpdate();
                }

                conn.commit();
                loadData();
            } catch (SQLException e) {
                if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                e.printStackTrace();
            } finally {
                if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    private void siapkanFormEdit() {
        formLabel.setText("Edit Ekstrakurikuler");
        fieldNama.setText(eskulTerpilih.getNama());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        try {
            LocalDate date = LocalDate.parse(eskulTerpilih.getJadwal(), formatter);
            datePickerJadwal.setValue(date);
        } catch (Exception e) {
            datePickerJadwal.setValue(null);
        }

        tombolBatal.setVisible(true);
    }

    @FXML
    void onBatalClicked() {
        resetForm();
    }

    private void resetForm() {
        eskulTerpilih = null;
        formLabel.setText("Tambah Ekstrakurikuler Baru");
        fieldNama.clear();
        datePickerJadwal.setValue(null);
        tombolBatal.setVisible(false);
    }

    @FXML
    void onBackToDashboardClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Dashboard Guru");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/guru/guru-view.fxml"));
            Parent root = loader.load();
            GuruViewController controller = loader.getController();
            controller.setUser(this.user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
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