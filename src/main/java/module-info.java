module com.example.bdsqltester {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.zaxxer.hikari;
    requires java.sql;
    requires org.slf4j;
    requires jdk.compiler;


    opens com.example.bdsqltester to javafx.fxml;
    exports com.example.bdsqltester;
    exports com.example.bdsqltester.datasources;
    opens com.example.bdsqltester.datasources to javafx.fxml;
    exports com.example.bdsqltester.scenes;
    opens com.example.bdsqltester.scenes to javafx.fxml;
    exports com.example.bdsqltester.scenes.admin;
    opens com.example.bdsqltester.scenes.admin to javafx.fxml;
    exports com.example.bdsqltester.scenes.siswa;
    opens com.example.bdsqltester.scenes.siswa to javafx.fxml;
    exports com.example.bdsqltester.scenes.guru;
    opens com.example.bdsqltester.scenes.guru to javafx.fxml;
    exports com.example.bdsqltester.scenes.walikelas;
    opens com.example.bdsqltester.scenes.walikelas to javafx.fxml;
    exports com.example.bdsqltester.dtos;
    opens com.example.bdsqltester.dtos to javafx.fxml;
}