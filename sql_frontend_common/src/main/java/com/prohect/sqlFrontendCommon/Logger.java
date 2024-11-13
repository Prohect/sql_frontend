package com.prohect.sqlFrontendCommon;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@SuppressWarnings("unused")
public class Logger {
    public static final String logFileSuffix = MessageFormat.format("{0}.log", datetime());
    public static final File logRoot = new File("log");
    private static final String logFileName = "Log-";
    public static Logger logger = new Logger("basic");

    static {
        if (!logRoot.exists()) {
            @SuppressWarnings("unused") boolean mkdir = logRoot.mkdir();
        }
    }

    public final File file;
    public final String logFilePrefix;
    private boolean initialized = false;

    public Logger(String logFilePrefix) {
        this.logFilePrefix = logFilePrefix;
        file = new File(logRoot, logFilePrefix + logFileName + logFileSuffix);
        logger = this;
    }

    public static String datetime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss"));
    }

    private void initialize() {
        if (initialized) return;
        initialized = true;
        try {
            @SuppressWarnings("unused") boolean newFile = file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * because this is the logger's log method, if there's exception, cannot try logging it, which may cause stack overflow
     */
    public void log(String message) {
        log2consoleLn(message);
        log2fileLn(message);
    }

    public void log(List<String> messages) {
        log2fileLn(messages);
        log2consoleLn(messages);
    }

    private String processMessage(String message) {
        return "[" + datetime() + "] " + message;
    }

    public void log2fileLn(String message) {
        initialize();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fc = raf.getChannel()) {
            raf.seek(raf.length());
            @SuppressWarnings("unused") int write = fc.write(ByteBuffer.wrap(processMessage(message + "\r\n").getBytes()));
            fc.force(false);
        } catch (Exception e) {
            System.err.println("Error logging exception: " + e.getMessage());
        }
    }

    public void log2fileLnConcurrent(List<String> messages) {
        new Thread(() -> log2fileLn(messages)).start();
    }

    public void log2fileLn(List<String> messages) {
        initialize();
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fc = raf.getChannel()) {
            raf.seek(raf.length());
            for (String message : messages) {
                @SuppressWarnings("unused") int write = fc.write(ByteBuffer.wrap(processMessage(processMessage(message) + "\r\n").getBytes()));
            }
            fc.force(false);
        } catch (Exception e) {
            System.err.println("Error logging exception: " + e.getMessage());
        }
    }

    public void log2consoleLnConcurrent(List<String> messages) {
        new Thread(() -> log2consoleLn(messages)).start();
    }

    public void log2consoleLn(List<String> messages) {
        initialize();
        for (String s : messages) log2consoleLn(processMessage(s));
    }

    public void log2consoleLn(String message) {
        initialize();
        System.out.println(message);
    }

    public void log(Throwable e) {
        try (var s = new StringWriter(); var printWriter = new PrintWriter(s)) {
            e.printStackTrace(printWriter);
            printWriter.flush();
            log(s.toString());
        } catch (Exception ex) {
            System.err.println("Error logging exception: " + ex.getMessage());
        }
    }
}