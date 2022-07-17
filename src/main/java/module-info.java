module a8.jtagaca.javafxtodo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;


    opens a8.jtagaca.javafxtodo to javafx.fxml;
    exports a8.jtagaca.javafxtodo;
}