module com.example.Project {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.slf4j;
    requires jdk.compiler;
    requires kernel;
    requires layout;


    opens com.example.Project to javafx.fxml;
    exports com.example.Project;
    exports com.example.Project.datasources;
    opens com.example.Project.datasources to javafx.fxml;
    exports com.example.Project.scenes;
    opens com.example.Project.scenes to javafx.fxml;
    exports com.example.Project.scenes.admin;
    opens com.example.Project.scenes.admin to javafx.fxml;
    exports com.example.Project.scenes.siswa;
    opens com.example.Project.scenes.siswa to javafx.fxml;
    exports com.example.Project.scenes.guru;
    opens com.example.Project.scenes.guru to javafx.fxml;
    exports com.example.Project.scenes.walikelas;
    opens com.example.Project.scenes.walikelas to javafx.fxml;
    exports com.example.Project.dtos;
    opens com.example.Project.dtos to javafx.fxml;
}