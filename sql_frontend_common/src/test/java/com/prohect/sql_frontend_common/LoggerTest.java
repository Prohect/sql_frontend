package com.prohect.sql_frontend_common;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class LoggerTest {
    @Test
    public void main() throws IOException {
        Logger logger = new Logger("test");
        logger.log(new Exception("loop"));
        logger.log("map pls");
    }
}