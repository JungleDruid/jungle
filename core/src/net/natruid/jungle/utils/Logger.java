package net.natruid.jungle.utils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.utils.ObjectMap;
import net.natruid.jungle.core.Sky;

import java.time.LocalTime;

public final class Logger {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public int logLevel = Sky.jungle.isDebug() ? Application.LOG_DEBUG : Application.LOG_INFO;

    private final ObjectMap<String, Long> stopWatchMap = new ObjectMap<>();

    public void debug(String message, Throwable exception) {
        output(Application.LOG_DEBUG, exception, "DEBUG", ANSI_RESET, message);
    }

    public void debug(String message) {
        debug(message, null);
    }

    public void info(String message, Throwable exception) {
        output(Application.LOG_INFO, exception, "INFO", ANSI_RESET, message);
    }

    public void info(String message) {
        info(message, null);
    }

    public void warn(String message, Throwable exception) {
        output(Application.LOG_INFO, exception, "WARN", ANSI_YELLOW, message);
    }

    public void warn(String message) {
        warn(message, null);
    }

    public void error(String message, Throwable exception) {
        output(Application.LOG_ERROR, exception, "ERROR", ANSI_RED, message);
    }

    public void error(String message) {
        error(message, null);
    }

    public void startWatch(String name) {
        stopWatchMap.put(name, System.nanoTime());
    }

    public void stopWatch(String name) {
        long start = stopWatchMap.get(name);
        output(Application.LOG_DEBUG, null, "STOPWATCH", ANSI_GREEN,
            String.format("%s: %sms", name, (System.nanoTime() - start) / 1000000.0));
    }

    public void output(int logLevel, Throwable exception, String tag, String color, String message) {
        if (this.logLevel < logLevel) return;
        System.out.printf("%s[%s] [%s] %s%n", color, LocalTime.now(), tag, message);
        if (exception != null) exception.printStackTrace();
        System.out.print(ANSI_RESET);
    }
}
