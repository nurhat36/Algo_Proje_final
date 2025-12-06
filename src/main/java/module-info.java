module org.example.algo_proje {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.sql;
    requires java.desktop;

    opens org.example.algo_proje to javafx.fxml;
    exports org.example.algo_proje;
    exports org.example.algo_proje.Controllers;
    opens org.example.algo_proje.Controllers to javafx.fxml;
}