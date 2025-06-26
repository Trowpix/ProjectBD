package com.example.Project.scenes.guru;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.TableTampilanSiswa;
import com.example.Project.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InputTampilkanSiswa {

    @FXML
    private TableView<TableTampilanSiswa> siswaTable;
    @FXML
    private TableColumn<TableTampilanSiswa, String> namaSiswaColumn;
    @FXML
    private TableColumn<TableTampilanSiswa, String> nisColumn;
    @FXML
    private TableColumn<TableTampilanSiswa, String> jenisKelaminColumn;
    @FXML
    private Label kelasLabel;
    @FXML
    private Label waliLabel;
    
    private User user = new User();
    private String kelas;

    public void setUser(User user) {
        this.user = user;
    }

    public void setKelas(String kelas){
        this.kelas = kelas;
        kelasLabel.setText("Kelas: "+kelas);
        update();
    }

    @FXML
    void initialize(){
        namaSiswaColumn.setCellValueFactory(new PropertyValueFactory<>("nama"));
        nisColumn.setCellValueFactory(new PropertyValueFactory<>("nis"));
        jenisKelaminColumn.setCellValueFactory(new PropertyValueFactory<>("kelamin"));
    }

    void update(){
        ObservableList<TableTampilanSiswa> tampilan = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT k.nama_kelas, nama_guru  FROM kelas k JOIN wali_kelas wk ON k.id_kelas = wk.id_kelas JOIN guru g ON g.nip = wk.nip_guru WHERE k.nama_kelas = ?");
            stmt.setString(1,kelas);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                waliLabel.setText("Wali Kelas: "+rs.getString("nama_guru"));
            }

            stmt = data.prepareStatement("SELECT nama_siswa, nomor_induk_siswa, gender_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM kelas WHERE nama_kelas = UPPER(?)) ORDER BY nomor_induk_siswa");
            stmt.setString(1,kelas);
            rs = stmt.executeQuery();
            while (rs.next()){
                String nama = rs.getString("nama_siswa");
                String nis = rs.getString("nomor_induk_siswa");
                String kelamin = rs.getString("gender_siswa");
                tampilan.add(new TableTampilanSiswa(nama,nis,kelamin));
            }
            siswaTable.setItems(tampilan);

        }catch (SQLException e){
            System.out.println("Error updateNameLabelSQL : " + e);
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            app.getPrimaryStage().setTitle("Input Nilai");

            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-inputNilai.fxml"));
            Parent root = loader.load();

            InputNilaiController inputNilaiController = loader.getController();
            inputNilaiController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
