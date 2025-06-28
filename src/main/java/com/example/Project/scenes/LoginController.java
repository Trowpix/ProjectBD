package com.example.Project.scenes;

import com.example.Project.MainMenu;
import com.example.Project.datasources.MainDataSource;
import com.example.Project.dtos.User;
import com.example.Project.scenes.admin.AdminViewController;
import com.example.Project.scenes.guru.GuruViewController;
import com.example.Project.scenes.siswa.SiswaViewController;
import com.example.Project.scenes.walikelas.WaliKelasViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private ChoiceBox<String> selectRole;
    @FXML
    private ComboBox<String> idComboBox;
    @FXML
    private Button loginButton;

    private User user;
    private boolean wakiKelas = false;

    @FXML
    void initialize() {
        selectRole.getItems().addAll("Admin", "Siswa", "Guru");
        idComboBox.setDisable(true);

        selectRole.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                idComboBox.setDisable(false);
                populateIdComboBox(newValue);
            } else {
                idComboBox.setDisable(true);
                idComboBox.getItems().clear();
            }
        });
    }

    private void populateIdComboBox(String role) {
        ObservableList<String> idList = FXCollections.observableArrayList();
        String sql = "SELECT login_id FROM users WHERE role = ? ORDER BY login_id";

        try (Connection conn = MainDataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role.toLowerCase());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                idList.add(rs.getString("login_id"));
            }

            idComboBox.setItems(idList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    boolean verifyCredentials(String username, String password, String role) throws SQLException {
        if (username == null || password == null || role == null) return false;

        try (Connection data = MainDataSource.getConnection()){
            PreparedStatement stmt = data.prepareStatement("SELECT * FROM users WHERE login_id = ? AND role = ?");
            stmt.setString(1, username);
            stmt.setString(2, role.toLowerCase());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String dbPassword = rs.getString("password");
                if (dbPassword.equals(password)) {
                    if (role.equalsIgnoreCase("guru")) {
                        PreparedStatement waliStmt = data.prepareStatement("SELECT * FROM wali_kelas WHERE nip_guru = ? AND id_kelas IS NOT NULL");
                        waliStmt.setString(1, username);
                        ResultSet ts = waliStmt.executeQuery();
                        if (ts.next()) wakiKelas = true;
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
    void onLoginClick() {
        String id = idComboBox.getValue();
        String password = passwordField.getText();
        String role = selectRole.getValue();

        if (id == null || password.isEmpty() || role == null || role.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Input Tidak Lengkap", "Harap pilih Peran, User ID, dan isi Password.");
            return;
        }

        try {
            if (verifyCredentials(id, password, role)) {
                MainMenu app = MainMenu.getApplicationInstance();
                String fxmlFile = "";
                String title = "";

                if (role.equals("Admin")) {
                    fxmlFile = "fxml/admin/admin-view.fxml";
                    title = "Admin Dashboard";
                } else if (role.equals("Siswa")) {
                    fxmlFile = "fxml/siswa/siswa-view.fxml";
                    title = "Siswa Dashboard";
                } else if (role.equals("Guru")) {
                    if (wakiKelas) {
                        fxmlFile = "fxml/guru/guru-walikelasView.fxml";
                        title = "Wali Kelas Dashboard";
                    } else {
                        fxmlFile = "fxml/guru/guru-view.fxml";
                        title = "Guru Dashboard";
                    }
                }

                loadScene(fxmlFile, title);

            } else {
                showAlert(Alert.AlertType.ERROR, "Login Gagal", "User ID atau Password salah.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Tidak dapat terhubung ke database.");
            e.printStackTrace();
        }
    }

    private void loadScene(String fxmlFile, String title) {
        try {
            MainMenu app = MainMenu.getApplicationInstance();
            FXMLLoader loader = new FXMLLoader(MainMenu.class.getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminViewController) ((AdminViewController) controller).setUser(user);
            if (controller instanceof SiswaViewController) ((SiswaViewController) controller).setUser(user);
            if (controller instanceof GuruViewController) ((GuruViewController) controller).setUser(user);
            if (controller instanceof WaliKelasViewController) ((WaliKelasViewController) controller).setUser(user);

            Scene scene = new Scene(root);

            if (fxmlFile.equals("login-view.fxml")) {
                scene.getStylesheets().add(MainMenu.class.getResource("login-styles.css").toExternalForm());
            }

            app.getPrimaryStage().setTitle(title);
            app.getPrimaryStage().setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException(e);
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