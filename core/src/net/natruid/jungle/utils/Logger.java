package net.natruid.jungle.utils;

import com.badlogic.gdx.Application;
import net.natruid.jungle.core.Jungle;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public final class Logger {
    public static final int logLevel = Jungle.Companion.getDebug() ? Application.LOG_DEBUG : Application.LOG_INFO;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";

    private static final Map<String, Long> stopWatchMap = new HashMap<>();

    public static void debug(String message, Throwable exception) {
        output(Application.LOG_DEBUG, exception, "DEBUG", ANSI_RESET, message);
    }

    public static void debug(String message) {
        debug(message, null);
    }

    public static void info(String message, Throwable exception) {
        output(Application.LOG_INFO, exception, "INFO", ANSI_RESET, message);
    }

    public static void info(String message) {
        info(message, null);
    }

    public static void warn(String message, Throwable exception) {
        output(Application.LOG_INFO, exception, "WARN", ANSI_YELLOW, message);
    }

    public static void warn(String message) {
        warn(message, null);
    }

    public static void error(String message, Throwable exception) {
        output(Application.LOG_ERROR, exception, "ERROR", ANSI_RED, message);
    }

    public static void error(String message) {
        error(message, null);
    }

    public static void startWatch(String name) {
        stopWatchMap.put(name, System.nanoTime());
    }

    public static void stopWatch(String name) {
        long start = stopWatchMap.get(name);
        output(Application.LOG_DEBUG, null, "STOPWATCH", ANSI_GREEN,
            String.format("%s: %sms", name, (System.nanoTime() - start) / 1000000.0));
    }

    public static void output(int logLevel, Throwable exception, String tag, String color, String message) {
        if (Logger.logLevel < logLevel) return;
        System.out.printf("%s[%s] [%s] %s%n", color, LocalTime.now(), tag, message);
        if (exception != null) exception.printStackTrace();
        System.out.print(ANSI_RESET);
    }
}
