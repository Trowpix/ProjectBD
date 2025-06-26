package com.example.projectbd.scenes.walikelas;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.TableViewGrade;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    @FXML
    private Label nilaiAkhirLabel;
    @FXML
    private Label kelasLabel;
    @FXML
    private Label waliLabel;
    @FXML
    private ComboBox<String> siswaComboBox;
    @FXML
    private ComboBox<String> tahunAjaranComboBox;
    @FXML
    private ComboBox<String> semesterComboBox;
    @FXML
    private Label namaSiswaLabel;
    @FXML
    private Label judulRaporLabel;
    @FXML
    private TableView<TableViewGrade> raporTable;
    @FXML
    private TableColumn<TableViewGrade, String> mapelColumn;
    @FXML
    private TableColumn<TableViewGrade, Double> utsColumn;
    @FXML
    private TableColumn<TableViewGrade, Double> uasColumn;
    @FXML
    private TableColumn<TableViewGrade, Double> rataRataColumn;
    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        initializeComboBox();
        initializeLabel();
    }

    @FXML
    void initialize(){
        mapelColumn.setCellValueFactory(new PropertyValueFactory<>("mapelName"));
        utsColumn.setCellValueFactory(new PropertyValueFactory<>("utsValue"));
        uasColumn.setCellValueFactory(new PropertyValueFactory<>("uasValue"));
        rataRataColumn.setCellValueFactory(new PropertyValueFactory<>("rataRataValue"));
    }

    void initializeLabel(){
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT nama_kelas FROM kelas WHERE id_kelas = (SELECT id_kelas FROM wali_kelas WHERE nip_guru = ?)");
            stmt.setString(1,user.id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                kelasLabel.setText("Kelas: "+rs.getString("nama_kelas"));
            }
            waliLabel.setText("Wali Kelas: "+user.username);
        }catch (SQLException e){
            System.out.println("Error initializeLabel "+e);
        }
    }

    void initializeComboBox(){
        ObservableList<String> tahunAjaranList = FXCollections.observableArrayList();
        ObservableList<String> nisList = FXCollections.observableArrayList();
        semesterComboBox.getItems().addAll("1","2");
        semesterComboBox.setValue("1");
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT DISTINCT tahun_ajaran FROM rapor");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tahunAjaranList.add(rs.getString("tahun_ajaran"));
            }
            stmt = data.prepareStatement("SELECT DISTINCT nomor_induk_siswa FROM siswa WHERE id_kelas = (SELECT id_kelas FROM wali_kelas WHERE nip_guru = ?) ORDER BY nomor_induk_siswa");
            stmt.setString(1, user.id);
            rs = stmt.executeQuery();
            while (rs.next()) {
                nisList.add(rs.getString("nomor_induk_siswa"));
            }
            tahunAjaranComboBox.setItems(tahunAjaranList);
            siswaComboBox.setItems(nisList);
        }catch (SQLException e){
            System.out.println("Error initializeComboBox "+e);
        }
    }

    @FXML
    void onKembaliClicked() {
        try {
            HelloApplication app = HelloApplication.getApplicationInstance();
            app.getPrimaryStage().setTitle("Wali Kelas View");

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-walikelasView.fxml"));
            Parent root = loader.load();

            WaliKelasViewController waliKelasController = loader.getController();
            waliKelasController.setUser(user);

            Scene scene = new Scene(root);
            app.getPrimaryStage().setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    void onCetakPdfClicked() {
        System.out.println("Gendeng");
    }
    @FXML
    void onGenerateClicked() {
        ObservableList<TableViewGrade> grades = FXCollections.observableArrayList();
        if (siswaComboBox.getValue()!=null && tahunAjaranComboBox.getValue()!= null && semesterComboBox!=null){
            try (Connection data = MainDataSource.getConnection()){
                PreparedStatement stmt = data.prepareStatement("SELECT nama_mata_pelajaran, uts, uas  FROM rapor r JOIN nilai_ujian n ON n.id_rapor = r.id_rapor JOIN mata_pelajaran mp ON mp.id_mata_pelajaran = n.id_mata_pelajaran WHERE r.nomor_induk_siswa = ? AND tahun_ajaran = ? AND r.semester = ? ORDER BY mp.id_mata_pelajaran");
                stmt.setString(1, siswaComboBox.getValue());
                stmt.setString(2, tahunAjaranComboBox.getValue());
                stmt.setString(3, semesterComboBox.getValue());
                ResultSet rs = stmt.executeQuery();
                double rataRataTotal = 0;
                int count = 0;
                while (rs.next()) {
                    String mapel = rs.getString("nama_mata_pelajaran");
                    double uts = rs.getInt("uts");
                    double uas = rs.getInt("uas");
                    double rataRata = (uts+uas)/2;
                    rataRataTotal += rataRata;
                    count += 1;
                    grades.add(new TableViewGrade(mapel,uts,uas, rataRata));
                }
                raporTable.setItems(grades);
                nilaiAkhirLabel.setText(String.valueOf(rataRataTotal/count));

                stmt = data.prepareStatement("SELECT * FROM siswa WHERE nomor_induk_siswa = ?");
                stmt.setString(1, siswaComboBox.getValue());
                rs = stmt.executeQuery();
                if (rs.next()){
                    namaSiswaLabel.setText(rs.getString("nama_siswa"));
                }
            }catch (SQLException e){
                System.out.println("Error GenerateClicked "+e);
            }
        }
    }
}
