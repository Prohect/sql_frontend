module com.prohect.sql_frontend {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.prohect.sql_frontend to javafx.fxml;
    exports com.prohect.sql_frontend;
}