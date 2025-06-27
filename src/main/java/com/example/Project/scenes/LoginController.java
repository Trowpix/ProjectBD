package com.example.Project.scenes;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import com.example.Project.scenes.admin.AdminViewController;
import com.example.Project.scenes.guru.GuruViewController;
import com.example.Project.scenes.siswa.SiswaViewController;
import com.example.Project.scenes.walikelas.WaliKelasViewController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField passwordField;

    @FXML
    private ChoiceBox<String> selectRole;

    @FXML
    private TextField idField;

    private User user;
    private boolean wakiKelas = false;

    boolean verifyCredentials(String username, String password, String role) throws SQLException {
        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM users WHERE login_id = ? AND role = ?");
            stmt.setString(1, username);
            stmt.setString(2, role.toLowerCase());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");

                if (dbPassword.equals(password)) {
                    if (role.equalsIgnoreCase("guru")) {
                        try {
                            stmt = data.prepareStatement("SELECT * FROM wali_kelas WHERE nip_guru = ?");
                            stmt.setString(1, username);

                            ResultSet ts = stmt.executeQuery();
                            if (ts.next()) wakiKelas = true;
                        } catch (SQLException e) {
                            return false;
                        }
                    }
                    user = new User(rs);
                    if (wakiKelas) user.role = "Wali Kelas";
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @FXML
    void initialize() {
        idField.setText("SD0");
        passwordField.setText("12");

        selectRole.getItems().addAll("Admin", "Siswa", "Guru");
        selectRole.setValue("");
    }

    @FXML
    void onLoginClick() {
        String id = idField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        try {
            if (verifyCredentials(id, password, role)) {
                MainMenu app = MainMenu.getApplicationInstance();

                if (role.equals("Admin")) {
                    app.getPrimaryStage().setTitle("Admin View");
                    FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("admin-view.fxml"));
                    Parent root = loader.load();
                    AdminViewController adminController = loader.getController();
                    adminController.setUser(user);

                    Scene scene = new Scene(root);
                    app.getPrimaryStage().setScene(scene);
                } else if (role.equals("Siswa")){
                    app.getPrimaryStage().setTitle("Siswa View");

                    FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("siswa-view.fxml"));
                    Parent root = loader.load();
                    SiswaViewController siswaViewController = loader.getController();
                    siswaViewController.setUser(user);

                    Scene scene = new Scene(root);
                    app.getPrimaryStage().setScene(scene);
                }  else if (role.equals("Guru")){
                    if (wakiKelas) {
                        app.getPrimaryStage().setTitle("Wali Kelas View");

                        FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-walikelasView.fxml"));
                        Parent root = loader.load();

                        WaliKelasViewController waliKelasController = loader.getController();
                        waliKelasController.setUser(user);

                        Scene scene = new Scene(root);
                        app.getPrimaryStage().setScene(scene);
                    }else {
                        app.getPrimaryStage().setTitle("Guru View");

                        FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource("guru-view.fxml"));
                        Parent root = loader.load();

                        GuruViewController guruController = loader.getController();
                        guruController.setUser(user);

                        Scene scene = new Scene(root);
                        app.getPrimaryStage().setScene(scene);
                    }
                }
            } else {
                // Show an error message
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Login Failed");
                alert.setHeaderText("Invalid Credentials");
                alert.setContentText("Please check your username and password.");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Database Connection Failed");
            alert.setContentText("Could not connect to the database. Please try again later.");
            alert.showAndWait();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
