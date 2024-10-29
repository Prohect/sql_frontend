module com.prohect.sql_frontend {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.transport;
    requires io.netty.common;
    requires io.netty.buffer;
    requires com.alibaba.fastjson2;
    requires sql.frontend.common;


    opens com.prohect.sql_frontend to javafx.fxml;
    opens com.prohect.sql_frontend.login to javafx.fxml;
    opens com.prohect.sql_frontend.main to javafx.fxml;
    opens com.prohect.sql_frontend.main.insert to javafx.fxml;
    opens com.prohect.sql_frontend.main.insertNewColumn to javafx.fxml;
    exports com.prohect.sql_frontend;
    exports com.prohect.sql_frontend.login;
}