package com.example.Project.scenes.siswa;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.Ekstrakurikuler;
import com.example.Project.dtos.User;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class SiswaPendaftaranEkskulController {

    @FXML private TableView<Ekstrakurikuler> tableViewEskul;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomNama;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomJadwal;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomPembina;
    @FXML private TableColumn<Ekstrakurikuler, Void> kolomAksi;

    private User user;
    private final ObservableList<Ekstrakurikuler> eskulList = FXCollections.observableArrayList();
    private final Set<String> eskulTerdaftarIds = new HashSet<>();

    public void setUser(User user) {
        this.user = user;
        loadData();
    }

    private void loadData() {
        if (user == null || user.id == null) return;

        // 1. Dapatkan semua ID eskul yang sudah diikuti siswa
        try (Connection conn = MainDataSource.getConnection()) {
            String checkSql = "SELECT id_ekskul FROM siswa_ekstrakurikuler WHERE nomor_induk_siswa = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, user.id);
            ResultSet rsCheck = checkStmt.executeQuery();
            while (rsCheck.next()) {
                eskulTerdaftarIds.add(rsCheck.getString("id_ekskul"));
            }

            // 2. Dapatkan semua eskul yang tersedia dari tabel utama
            String getSql = "SELECT id_ekskul, nama_ekskul, jadwal, nama_pembina FROM ekstrakurikuler";
            PreparedStatement getStmt = conn.prepareStatement(getSql);
            ResultSet rsGet = getStmt.executeQuery();
            while (rsGet.next()) {
                Ekstrakurikuler eskul = new Ekstrakurikuler(
                        rsGet.getString("id_ekskul"),
                        rsGet.getString("nama_ekskul"),
                        rsGet.getString("jadwal"),
                        rsGet.getString("nama_pembina")
                );
                // 3. Tandai eskul jika ID-nya ada di daftar yang sudah diikuti
                if (eskulTerdaftarIds.contains(eskul.getId())) {
                    eskul.setTerdaftar(true);
                }
                eskulList.add(eskul);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tableViewEskul.setItems(eskulList);
    }

    @FXML
    public void initialize() {
        kolomNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kolomJadwal.setCellValueFactory(new PropertyValueFactory<>("jadwal"));
        kolomPembina.setCellValueFactory(new PropertyValueFactory<>("pembina"));
        addAksiButtonToTable();
    }

    private void addAksiButtonToTable() {
        Callback<TableColumn<Ekstrakurikuler, Void>, TableCell<Ekstrakurikuler, Void>> cellFactory = param -> {
            return new TableCell<>() {
                private final Button btn = new Button();
                {
                    btn.setOnAction(event -> {
                        Ekstrakurikuler eskul = getTableView().getItems().get(getIndex());
                        daftarEskul(eskul);
                    });
                }

                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Ekstrakurikuler eskul = getTableView().getItems().get(getIndex());

                        btn.textProperty().bind(Bindings.when(eskul.terdaftarProperty())
                                .then("Terdaftar")
                                .otherwise("Daftar"));
                        btn.disableProperty().bind(eskul.terdaftarProperty());
                        setGraphic(btn);
                    }
                }
            };
        };
        kolomAksi.setCellFactory(cellFactory);
    }

    private void daftarEskul(Ekstrakurikuler eskul) {
        String sql = "INSERT INTO siswa_ekstrakurikuler (nomor_induk_siswa, id_ekskul) VALUES (?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.id);

            // PERBAIKAN: Mengubah String ID menjadi Integer sebelum dikirim ke database
            pstmt.setInt(2, Integer.parseInt(eskul.getId()));

            pstmt.executeUpdate();

            // Update status di UI secara langsung
            eskul.setTerdaftar(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Pendaftaran Berhasil");
            alert.setHeaderText(null);
            alert.setContentText("Kamu berhasil terdaftar di ekstrakurikuler " + eskul.getNama());
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Pendaftaran Gagal");
            alert.setHeaderText(null);
            alert.setContentText("Terjadi kesalahan saat mendaftar.");
            alert.showAndWait();
        }
    }

    @FXML
    void onBackToDashboardClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Dashboard Siswa");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("siswa-view.fxml"));
            Parent root = loader.load();
            SiswaViewController controller = loader.getController();
            controller.setUser(this.user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
