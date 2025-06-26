package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.MainMenu;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SiswaBiodataController {

    private User user = new User();

    @FXML
    private Label namaLengkap;
    @FXML
    private Label tempatTanggalLahir;
    @FXML
    private Label nomorIndukSiswa;
    @FXML
    private Label jenisKelamin;
    @FXML
    private Label agama;
    @FXML
    private Label nomorTelepon;
    @FXML
    private Label alamat;
    @FXML
    private Label golonganDarah;

    public void setUser(User user) {
        this.user = user;
        updateBioPribadi();
    }

    @FXML
    void initialize(){
        updateBioPribadi();
    }
    void updateBioPribadi(){
        try (Connection data = MainDataSource.getConnection()){
            if(user.id!=null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE nomor_induk_siswa = ?");
                stmt.setString(1, user.id);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    namaLengkap.setText(user.username);
                    tempatTanggalLahir.setText(rs.getString("tempat_lahir_siswa") +", "+rs.getString("tanggal_lahir_siswa"));
                    nomorIndukSiswa.setText(rs.getString("nomor_induk_siswa"));
                    jenisKelamin.setText(rs.getString("gender_siswa"));
                    agama.setText(rs.getString("agama"));
                    alamat.setText(rs.getString("alamat"));
                    nomorTelepon.setText(rs.getString("no_hp"));
                    golonganDarah.setText(rs.getString("golongan_darah"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error updateBioPribadiSql");
        }
    }
    @FXML
    void onKembaliClicked() {
        try {
        MainMenu app = MainMenu.getApplicationInstance();
        app.getPrimaryStage().setTitle("Siswa View");

        FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("siswa-view.fxml"));
        Parent root = loader.load();
        SiswaViewController siswaViewController = loader.getController();
        siswaViewController.setUser(user);
        Scene scene = new Scene(root);
        app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
