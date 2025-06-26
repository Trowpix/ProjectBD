package com.example.projectbd.scenes.siswa;

import com.example.bdsqltester.HelloApplication;
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
import java.util.ArrayList;

public class DataOrangTuaController {

    @FXML
    private Label namaOrangTua;
    @FXML
    private Label statusOrangTua;
    @FXML
    private Label pekerjaanOrangTua;
    @FXML
    private Label nomorTelponOrangTua;
    @FXML
    private Label emailOrangTua;
    @FXML
    private Label alamatOrangTua;

    private int index ;
    private ArrayList<Integer> idOrtu;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        update();
        index = idOrtu.indexOf(idOrtu.getFirst());
        updateOrangTua(index);
    }
    @FXML
    void initialize(){
        idOrtu = new ArrayList<>();
    }

    void update() {
        try(Connection data = MainDataSource.getConnection()) {
            if(user.id!=null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM orang_tua WHERE nomor_induk_siswa = ?");
                stmt.setString(1, user.id);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()){
                    idOrtu.add(rs.getInt("id_orang_tua"));
                }
            }
        }catch (SQLException e){
            System.out.println("Error update SQL: "+e);
        }
    }

    void updateOrangTua(int a){
        try(Connection data = MainDataSource.getConnection()) {
            if(!idOrtu.isEmpty()) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM orang_tua WHERE id_orang_tua = ?");
                stmt.setInt(1, idOrtu.get(a));
                ResultSet rs = stmt.executeQuery();

                if (rs.next()){
                    namaOrangTua.setText(rs.getString("nama_orang_tua"));
                    statusOrangTua.setText(rs.getString("status_orang_tua"));
                    pekerjaanOrangTua.setText("");
                    nomorTelponOrangTua.setText(rs.getString("no_hp_orang_tua"));
                    emailOrangTua.setText(rs.getString("email_orang_tua"));
                    alamatOrangTua.setText(rs.getString("alamat_orang_tua"));
                }
            }
        }catch (SQLException e){
            System.out.println("Error SQL updateOrangTua:"+e);
        }
    }

    @FXML
    void onPrevClicked() {
        if (index+1 < idOrtu.size()) index++;
        else index = 0;
        updateOrangTua(index);
    }

    @FXML
    void onNextClicked() {
        if (index-1 >= 0) index--;
        else index = idOrtu.size()-1;
        updateOrangTua(index);
    }
    
    @FXML
    void onKembaliClicked() {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();

            app.getPrimaryStage().setTitle("Siswa Biodata");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-biodata.fxml"));
            Parent root = loader.load();
            SiswaBiodataController siswaBiodataController = loader.getController();
            siswaBiodataController.setUser(user);
            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
