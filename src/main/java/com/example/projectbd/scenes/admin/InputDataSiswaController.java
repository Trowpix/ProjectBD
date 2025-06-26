package com.example.projectbd.scenes.admin;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;

public class InputDataSiswaController {
    @FXML
    private TextField namaLengkapField;
    @FXML
    private TextField tempatLahirField;
    @FXML
    private DatePicker tanggalLahirPicker;
    @FXML
    private ChoiceBox<String> jenisKelaminChoice;
    @FXML
    private ChoiceBox<String> agamaChoice;
    @FXML
    private TextArea alamatField;
    @FXML
    private TextField nomorTeleponField;
    @FXML
    private ChoiceBox<String> golonganDarahChoice;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    void initialize() {
        jenisKelaminChoice.getItems().addAll("L","P");
        jenisKelaminChoice.setValue(null);

        agamaChoice.getItems().addAll("Islam", "Kristen", "Katolik", "Hindu", "Buddha", "Konghucu");
        agamaChoice.setValue(null);

        golonganDarahChoice.getItems().addAll("A", "B", "AB", "O");
        golonganDarahChoice.setValue(null);
        tanggalLahirPicker.setEditable(false);
    }

    @FXML
    void onResetFormClicked() {
        namaLengkapField.setText(null);
        tempatLahirField.setText(null);
        tanggalLahirPicker.setValue(null);
        jenisKelaminChoice.setValue(null);
        agamaChoice.setValue(null);
        alamatField.setText(null);
        nomorTeleponField.setText(null);
        golonganDarahChoice.setValue(null);
    }

    @FXML
    void onSimpanDataClicked() {
        String nip = "SD";
        if (cekData()){
            try(Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT nomor_induk_siswa FROM siswa ORDER BY nomor_induk_siswa DESC LIMIT 1");
                ResultSet rs = stmt.executeQuery();
                String no = "";
                if (rs.next()){
                     no += (Integer.parseInt(rs.getString("nomor_induk_siswa").substring(2)) + 1);
                }
                if (no.length()<3){
                    no = "0"+no;
                }
                nip+=no;

                stmt = data.prepareStatement("INSERT INTO users (login_id, password, role) VALUES (?, ?, ?); INSERT INTO siswa (nomor_induk_siswa, nama_siswa, tempat_lahir_siswa, tanggal_lahir_siswa, gender_siswa, agama, alamat, no_hp, golongan_darah) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
                stmt.setString(1,nip);
                stmt.setString(2, String.valueOf(tanggalLahirPicker.getValue()));
                stmt.setString(3,"siswa");
                stmt.setString(4, nip);
                stmt.setString(5, namaLengkapField.getText());
                stmt.setString(6, tempatLahirField.getText());
                stmt.setDate(7, Date.valueOf(tanggalLahirPicker.getValue()));
                stmt.setString(8, jenisKelaminChoice.getValue());
                stmt.setString(9, agamaChoice.getValue());
                stmt.setString(10, alamatField.getText());
                stmt.setString(11, nomorTeleponField.getText());
                stmt.setString(12, golonganDarahChoice.getValue());

                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Data berhasil");
                alert.setHeaderText("Data telah berhasil dimasukan");
                alert.showAndWait();
            }catch (SQLException e){
                System.out.println("Error: "+e);
            }
        }
    }

    boolean cekData(){
        if (namaLengkapField.getText() == null || tempatLahirField.getText() == null || tanggalLahirPicker.getValue() == null || jenisKelaminChoice.getValue() == null || agamaChoice.getValue() == null || alamatField.getText() == null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Data Belum Terisi");
            alert.setContentText("Data tidak dapat dimasukan karena belum terisi.");
            alert.showAndWait();
            return false;
        }
        try(Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM siswa WHERE nama_siswa = ? AND tanggal_lahir_siswa = ? AND gender_siswa = ? AND agama = ? AND golongan_darah = ? AND UPPER(tempat_lahir_siswa) = ?");
            stmt.setString(1, namaLengkapField.getText());
            stmt.setDate(2, Date.valueOf(tanggalLahirPicker.getValue()));
            stmt.setString(3, jenisKelaminChoice.getValue());
            stmt.setString(4, agamaChoice.getValue());
            stmt.setString(5, golonganDarahChoice.getValue());
            stmt.setString(6,tempatLahirField.getText().toUpperCase());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Siswa Exist");
                alert.setContentText("Data tidak dapat dimasukan karena nama, tempat/tanggal lahir, kelamin, agama dan golongan darah siswa sudah ada");
                alert.showAndWait();
                return false;
            }
        }catch (SQLException e){
            System.out.println("Error: "+e);
            return false;
        }
        return true;
    }

    @FXML
    void onKembaliClicked() {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Admin View");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
            Parent root = loader.load();
            AdminViewController adminController = loader.getController();
            adminController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
