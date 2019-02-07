package net.natruid.jungle.utils

import com.badlogic.gdx.Application
import net.natruid.jungle.core.Jungle
import java.time.LocalTime

object Logger {
    var logLevel = if (Jungle.debug) Application.LOG_DEBUG else Application.LOG_INFO

    const val ANSI_RESET = "\u001B[0m"
    const val ANSI_RED = "\u001B[31m"
    const val ANSI_YELLOW = "\u001B[33m"
    const val ANSI_GREEN = "\u001B[32m"

    inline fun debug(
        exception: Throwable? = null,
        tag: String = "DEBUG",
        color: String = ANSI_RESET,
        message: () -> String
    ) {
        if (logLevel < Application.LOG_DEBUG) return
        output(exception, tag, color, message)
    }

    inline fun info(
        exception: Throwable? = null,
        tag: String = "INFO",
        color: String = ANSI_RESET,
        message: () -> String
    ) {
        if (logLevel < Application.LOG_INFO) return
        output(exception, tag, color, message)
    }

    inline fun warn(
        exception: Throwable? = null,
        tag: String = "WARNING",
        color: String = ANSI_YELLOW,
        message: () -> String
    ) {
        if (logLevel < Application.LOG_INFO) return
        output(exception, tag, color, message)
    }

    inline fun error(
        exception: Throwable? = null,
        tag: String = "ERROR",
        color: String = ANSI_RED,
        message: () -> String
    ) {
        if (logLevel < Application.LOG_ERROR) return
        output(exception, tag, color, message)
    }

    inline fun catch(message: String, block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            error(e) { message }
        }
    }

    inline fun stopwatch(name: String, block: () -> Unit) {
        val start = System.nanoTime()
        block()
        val elapsedTime = System.nanoTime() - start
        debug(tag = "STOPWATCH", color = ANSI_GREEN) {
            "$name: ${elapsedTime / 1000000.0}ms"
        }
    }

    inline fun output(exception: Throwable?, tag: String, color: String, message: () -> String) {
        println("$color[${LocalTime.now()}] [$tag] ${message()}")
        exception?.printStackTrace()
        print(ANSI_RESET)
    }
}
