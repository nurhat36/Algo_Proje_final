module org.example.algo_proje {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.algo_proje to javafx.fxml;
    exports org.example.algo_proje;
}