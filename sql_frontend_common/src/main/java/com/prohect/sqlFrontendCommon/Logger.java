package com.prohect.sqlFrontendCommon;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@SuppressWarnings("unused")
public class Logger {
    private static final String logFileSuffix = MessageFormat.format("{0}.log", datetime());
    private static final File logRoot = new File("log");
    private static final String logFileName = "Log-";
    public static Logger logger = new Logger("basic");
    private static final ScheduledExecutorService logExecutorService = new ScheduledThreadPoolExecutor(1);

    static {
        if (!logRoot.exists()) {
            @SuppressWarnings("unused") boolean mkdir = logRoot.mkdir();
        }
    }

    private final File file;
    private boolean initialized = false;

    public Logger(String logFilePrefix) {
        file = new File(logRoot, logFilePrefix + logFileName + logFileSuffix);
        logger = this;
    }

    private static String datetime() {
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
        logExecutorService.schedule(() -> {
            initialize();
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw"); FileChannel fc = raf.getChannel()) {
                raf.seek(raf.length());
                @SuppressWarnings("unused") int write = fc.write(ByteBuffer.wrap(processMessage(message + "\r\n").getBytes()));
                fc.force(false);
            } catch (Exception e) {
                System.err.println("Error logging exception: " + e.getMessage());
            }
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);
    }


    public void log2fileLn(List<String> messages) {
        logExecutorService.schedule(() -> {
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
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void log2consoleLn(List<String> messages) {
        logExecutorService.schedule(() -> {
            initialize();
            for (String message : messages) System.out.println(message);
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    public void log2consoleLn(String message) {
        logExecutorService.schedule(() -> {
            initialize();
            System.out.println(message);
        }, 0, java.util.concurrent.TimeUnit.MILLISECONDS);
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