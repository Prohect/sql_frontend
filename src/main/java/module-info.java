module com.prohect.sql_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.netty.buffer;
    requires com.alibaba.fastjson2;
    requires sql.frontend.common;


    opens com.prohect.sqlFrontend to javafx.fxml;
    opens com.prohect.sqlFrontend.main to javafx.fxml;
    opens com.prohect.sqlFrontend.main.login to javafx.fxml;
    opens com.prohect.sqlFrontend.main.newRow to javafx.fxml;
    opens com.prohect.sqlFrontend.main.newColumn to javafx.fxml;
    exports com.prohect.sqlFrontend;
    exports com.prohect.sqlFrontend.main;
    exports com.prohect.sqlFrontend.main.login;
    exports com.prohect.sqlFrontend.main.newRow;
    exports com.prohect.sqlFrontend.main.newColumn;
}