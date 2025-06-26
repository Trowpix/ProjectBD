package com.example.projectbd.scenes;

import com.example.bdsqltester.HelloApplication;
import com.example.bdsqltester.datasources.MainDataSource;
import com.example.bdsqltester.dtos.User;
import com.example.bdsqltester.scenes.admin.AdminViewController;
import com.example.bdsqltester.scenes.guru.GuruViewController;
import com.example.bdsqltester.scenes.siswa.SiswaViewController;
import com.example.bdsqltester.scenes.walikelas.WaliKelasViewController;
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
        // Call the database to verify the credentials
        // This is insecure as this stores the password in plain text.
        // In a real application, you should hash the password and store it securely.

        // Get a connection to the database
        try (Connection data = MainDataSource.getConnection()){
            // Create a prepared statement to prevent SQL injection
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM users WHERE login_id = ? AND role = ?");
            stmt.setString(1, username);
            stmt.setString(2, role.toLowerCase());

            // Execute the query
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // User found, check the password
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
                    return true; // Credentials are valid
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // If we reach here, the credentials are invalid
        return false;
    }

    @FXML
    void initialize() {
        idField.setText("G001");
        passwordField.setText("guru123");

        selectRole.getItems().addAll("Admin", "Siswa", "Guru");
        selectRole.setValue("Guru");
    }

    @FXML
    void onLoginClick() {
        // Get the username and password from the text fields
        String id = idField.getText();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        // Verify the credentials
        try {
            if (verifyCredentials(id, password, role)) {
                HelloApplication app = HelloApplication.getApplicationInstance();
                // Load the correct view based on the role
                if (role.equals("Admin")) {
                    // Load the admin view
                    app.getPrimaryStage().setTitle("Admin View");

                    // Load fxml and set the scene
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
                    Parent root = loader.load();
                    //pass the user to the next controller
                    AdminViewController adminController = loader.getController();
                    adminController.setUser(user);

                    Scene scene = new Scene(root);
                    app.getPrimaryStage().setScene(scene);
                } else if (role.equals("Siswa")){
                    // Load the Siswa view
                    app.getPrimaryStage().setTitle("Siswa View");

                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("siswa-view.fxml"));
                    Parent root = loader.load();
                    //pass the user to the next controller
                    SiswaViewController siswaViewController = loader.getController();
                    siswaViewController.setUser(user);

                    Scene scene = new Scene(root);
                    app.getPrimaryStage().setScene(scene);
                }  else if (role.equals("Guru")){
                    if (wakiKelas) {
                        app.getPrimaryStage().setTitle("Wali Kelas View");

                        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-walikelasView.fxml"));
                        Parent root = loader.load();

                        WaliKelasViewController waliKelasController = loader.getController();
                        waliKelasController.setUser(user);

                        Scene scene = new Scene(root);
                        app.getPrimaryStage().setScene(scene);
                    }else {
                        app.getPrimaryStage().setTitle("Guru View");

                        FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("guru-view.fxml"));
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
