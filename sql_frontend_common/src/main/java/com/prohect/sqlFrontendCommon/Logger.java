package com.prohect.sqlFrontendCommon;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public static final String logFileSuffix = MessageFormat.format("{0}.log", datetime());
    public static final File logRoot = new File("log");
    private static final String logFileName = "Log-";
    public static Logger logger;

    static {
        logger = new Logger("basic");
    }

    public final String logFilePrefix;
    private FileChannel logFileChannel;
    private boolean initialized = false;

    public Logger(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        logger = this;
    }

    public static String datetime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss"));
    }

    private void init() throws IOException {
        if (!initialized) {
            if (!logRoot.exists()) {
                @SuppressWarnings("unused") boolean mkdir = logRoot.mkdir();
            }
            File logFile = new File(logRoot, logFilePrefix + logFileName + logFileSuffix);
            @SuppressWarnings("unused") boolean newFile = logFile.createNewFile();
            logFileChannel = FileChannel.open(logFile.toPath(), StandardOpenOption.WRITE);
            initialized = true;
        }
    }

    /**
     * because this is the logger's log method, if there's exception, cannot try logging it, which may cause stack overflow
     */
    @SuppressWarnings("CallToPrintStackTrace")
    public void log(String... message) {
        String datetime = datetime();
        StringBuilder builder = new StringBuilder("[%s]".formatted(datetime));//automatically add time
        for (String string : message) {
            builder.append(string);
        }
        builder.append("\r\n");
        System.out.print(builder);
        try {
            init();
            @SuppressWarnings("unused") int write = logFileChannel.write(ByteBuffer.wrap(builder.toString().getBytes()));
            logFileChannel.force(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void log(Throwable e) {
        StringWriter s = new StringWriter();
        PrintWriter printWriter = new PrintWriter(s);
        e.printStackTrace(printWriter);
        printWriter.flush();
        log(s.toString());
        printWriter.close();
    }
}