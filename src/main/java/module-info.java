module dynoplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jsoup;

    opens dynoplayer to javafx.fxml;
    exports dynoplayer;
}
