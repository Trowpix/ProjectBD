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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class GuruManajemenEkskulController {

    @FXML private TableView<Ekstrakurikuler> tableViewEskul;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomNama;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomJadwal;
    @FXML private TableColumn<Ekstrakurikuler, String> kolomPembina;
    @FXML private TableColumn<Ekstrakurikuler, Void> kolomAksi;

    @FXML private Label formLabel;
    @FXML private TextField fieldNama;
    @FXML private TextField fieldJadwal;
    @FXML private TextField fieldPembina;
    @FXML private Button tombolSimpan;
    @FXML private Button tombolBatal;

    private User user;
    private final ObservableList<Ekstrakurikuler> eskulList = FXCollections.observableArrayList();
    private Ekstrakurikuler eskulTerpilih = null;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    public void initialize() {
        kolomNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kolomJadwal.setCellValueFactory(new PropertyValueFactory<>("jadwal"));
        kolomPembina.setCellValueFactory(new PropertyValueFactory<>("pembina"));
        addAksiButtonsToTable();
        loadData();
        resetForm();
    }

    private void loadData() {
        eskulList.clear();
        String sql = "SELECT id_ekskul, nama_ekskul, jadwal, nama_pembina FROM ekstrakurikuler";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                eskulList.add(new Ekstrakurikuler(
                        rs.getString("id_ekskul"),
                        rs.getString("nama_ekskul"),
                        rs.getString("jadwal"),
                        rs.getString("nama_pembina")
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
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                    }
                }
            };
        };
        kolomAksi.setCellFactory(cellFactory);
    }

    @FXML
    void onSimpanClicked() {
        if (eskulTerpilih == null) {
            tambahEskul();
        } else {
            updateEskul();
        }
    }

    @FXML
    void onBatalClicked() {
        resetForm();
    }

    private void tambahEskul() {
        String sql = "INSERT INTO ekstrakurikuler (nama_ekskul, jadwal, nama_pembina) VALUES (?, ?, ?)";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fieldNama.getText());
            pstmt.setString(2, fieldJadwal.getText());
            pstmt.setString(3, fieldPembina.getText());
            pstmt.executeUpdate();
            loadData();
            resetForm();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateEskul() {
        String sql = "UPDATE ekstrakurikuler SET nama_ekskul = ?, jadwal = ?, nama_pembina = ? WHERE id_ekskul = ?";
        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fieldNama.getText());
            pstmt.setString(2, fieldJadwal.getText());
            pstmt.setString(3, fieldPembina.getText());

            // PERBAIKAN: Mengubah String ID menjadi Integer sebelum dikirim ke database
            pstmt.setInt(4, Integer.parseInt(eskulTerpilih.getId()));

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
        alert.setContentText("Tindakan ini akan menghapus semua data pendaftaran siswa pada eskul ini.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String sql = "DELETE FROM ekstrakurikuler WHERE id_ekskul = ?";
            try (Connection conn = MainDataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                // PERBAIKAN: Mengubah String ID menjadi Integer sebelum dikirim ke database
                pstmt.setInt(1, Integer.parseInt(eskul.getId()));

                pstmt.executeUpdate();
                loadData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void siapkanFormEdit() {
        formLabel.setText("Edit Ekstrakurikuler");
        fieldNama.setText(eskulTerpilih.getNama());
        fieldJadwal.setText(eskulTerpilih.getJadwal());
        fieldPembina.setText(eskulTerpilih.getPembina());
        tombolBatal.setVisible(true);
    }

    private void resetForm() {
        eskulTerpilih = null;
        formLabel.setText("Tambah Ekstrakurikuler Baru");
        fieldNama.clear();
        fieldJadwal.clear();
        fieldPembina.clear();
        tombolBatal.setVisible(false);
    }

    @FXML
    void onBackToDashboardClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Dashboard Guru");
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-view.fxml"));
            Parent root = loader.load();
            GuruViewController controller = loader.getController();
            controller.setUser(this.user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
