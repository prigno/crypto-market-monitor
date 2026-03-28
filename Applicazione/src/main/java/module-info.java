module it.unipi.applicazione {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires org.apache.logging.log4j;
    
    opens it.unipi.applicazione to javafx.fxml, com.google.gson;
    exports it.unipi.applicazione;
}
