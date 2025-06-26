package com.example.bdsqltester.scenes.siswa;

import com.example.bdsqltester.MainMenu;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.TableViewGrade;
import com.example.bdsqltester.dtos.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class SiswaGradeController {


    @FXML
    private TableView<TableViewGrade> gradesTable;
    @FXML
    private TableColumn<TableViewGrade, String> mataPelajaranColumn = new TableColumn<>();
    @FXML
    private TableColumn<TableViewGrade, Double> utsGradeColumn = new TableColumn<>();
    @FXML
    private TableColumn<TableViewGrade, Double> uasGradeColumn = new TableColumn<>();
    @FXML
    private TableColumn<TableViewGrade, Double> rataRataColumn = new TableColumn<>();
    @FXML
    private ChoiceBox<String> semesterChoice;
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label classLabel;
    @FXML
    private Label averageGradeLabel;
    @FXML
    private Label predicateLabel;
    @FXML
    private Label classRankLabel;

    private User user = new User();

    public void setUser(User user) {
        this.user = user;
        update();
    }

    @FXML
    void initialize(){
        semesterChoice.getItems().addAll("1", "2");
        semesterChoice.setValue("1");
        semesterChoice.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                update();
            }
        });

        mataPelajaranColumn.setCellValueFactory(new PropertyValueFactory<>("mapelName"));
        utsGradeColumn.setCellValueFactory(new PropertyValueFactory<>("utsValue"));
        uasGradeColumn.setCellValueFactory(new PropertyValueFactory<>("uasValue"));
        rataRataColumn.setCellValueFactory(new PropertyValueFactory<>("rataRataValue"));
    }

    void update(){
        ObservableList<TableViewGrade> grades = FXCollections.observableArrayList();
        try (Connection data = MainDataSource.getConnection()){
            if (user.id != null) {
                PreparedStatement stmt = data.prepareStatement("SELECT * FROM kelas WHERE id_kelas = (SELECT id_kelas FROM siswa WHERE nomor_induk_siswa = ?)");
                stmt.setString(1, user.id);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    studentNameLabel.setText("Nilai Akademik "+user.username);
                    classLabel.setText("Kelas : "+rs.getString("nama_kelas"));
                }
                double rataRataTotal = 0;
                int count = 0;
                stmt = data.prepareStatement("SELECT nama_mata_pelajaran, uts, uas FROM nilai_ujian nilai JOIN mata_pelajaran mapel ON nilai.id_mata_pelajaran = mapel.id_mata_pelajaran WHERE nomor_induk_siswa = ? AND semester = ?");
                stmt.setString(1, user.id);
                stmt.setString(2, semesterChoice.getValue());

                rs = stmt.executeQuery();
                while (rs.next()) {
                    String mapel = rs.getString("nama_mata_pelajaran");
                    double uts = rs.getInt("uts");
                    double uas = rs.getInt("uas");
                    double rataRata = (uts+uas)/2;
                    rataRataTotal += rataRata;
                    count += 1;
                    grades.add(new TableViewGrade(mapel,uts,uas, rataRata));
                }

                gradesTable.setItems(grades);
                double average = rataRataTotal/count;
                averageGradeLabel.setText(String.valueOf(average));
                String predikat = "D";
                if (average >= 93) predikat = "A";
                else if (average >= 84) predikat = "B";
                else if (average >= 75) predikat = "C";
                predicateLabel.setText(predikat);
            }
        } catch (SQLException e){
            System.out.println("Error updateNameLabelSQL");
        }
    }

    @FXML
    void onCetakLaporanClicked() {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
