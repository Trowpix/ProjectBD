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
import java.util.Optional;
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
        eskulList.clear();
        eskulTerdaftarIds.clear();

        try (Connection conn = MainDataSource.getConnection()) {
            String checkSql = "SELECT id_ekskul FROM siswa_ekstrakurikuler WHERE nomor_induk_siswa = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, user.id);
            ResultSet rsCheck = checkStmt.executeQuery();
            while (rsCheck.next()) {
                eskulTerdaftarIds.add(rsCheck.getString("id_ekskul"));
            }

            String getSql = "SELECT e.id_ekskul, e.nama_ekskul, e.jadwal, g.nama_guru " +
                    "FROM ekstrakurikuler e " +
                    "LEFT JOIN guru g ON e.nip_pembina = g.nip";
            PreparedStatement getStmt = conn.prepareStatement(getSql);
            ResultSet rsGet = getStmt.executeQuery();

            while (rsGet.next()) {
                Ekstrakurikuler eskul = new Ekstrakurikuler(
                        rsGet.getString("id_ekskul"),
                        rsGet.getString("nama_ekskul"),
                        rsGet.getString("jadwal"),
                        rsGet.getString("nama_guru")
                );
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
                        if (eskul.isTerdaftar()) {
                            batalDaftarEskul(eskul);
                        } else {
                            daftarEskul(eskul);
                        }
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
                                .then("Batal Daftar")
                                .otherwise("Daftar"));

                        btn.styleProperty().bind(Bindings.when(eskul.terdaftarProperty())
                                .then("-fx-background-color: #e74c3c; -fx-text-fill: white;")
                                .otherwise("-fx-background-color: #27ae60; -fx-text-fill: white;"));

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
            pstmt.setInt(2, Integer.parseInt(eskul.getId()));
            pstmt.executeUpdate();

            eskul.setTerdaftar(true);

            showAlert(Alert.AlertType.INFORMATION, "Pendaftaran Berhasil", "Kamu berhasil terdaftar di ekstrakurikuler " + eskul.getNama());

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Pendaftaran Gagal", "Terjadi kesalahan saat mendaftar.");
        }
    }

    private void batalDaftarEskul(Ekstrakurikuler eskul) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Konfirmasi Pembatalan");
        confirmation.setHeaderText("Anda yakin ingin batal mendaftar dari eskul '" + eskul.getNama() + "'?");
        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM siswa_ekstrakurikuler WHERE nomor_induk_siswa = ? AND id_ekskul = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, user.id);
                pstmt.setInt(2, Integer.parseInt(eskul.getId()));
                pstmt.executeUpdate();

                eskul.setTerdaftar(false);
                showAlert(Alert.AlertType.INFORMATION, "Pembatalan Berhasil", "Pendaftaran Anda di eskul " + eskul.getNama() + " telah dibatalkan.");

            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Gagal", "Terjadi kesalahan saat membatalkan pendaftaran.");
            }
        }
    }

    @FXML
    void onBackToDashboardClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Dashboard Siswa");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("fxml/siswa/siswa-view.fxml"));
            Parent root = loader.load();
            SiswaViewController controller = loader.getController();
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