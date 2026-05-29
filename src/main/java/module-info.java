module dynoplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires org.jsoup;

    opens dynoplayer to javafx.fxml;

    exports dynoplayer;
}
