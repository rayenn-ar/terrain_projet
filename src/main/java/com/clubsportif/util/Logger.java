package com.clubsportif.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger console simple pour tracer les événements applicatifs.
 */
public final class Logger {

    private Logger() {}

    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        System.out.println(ConsoleColors.CYAN + "[INFO]  " + now() + " - " + message + ConsoleColors.RESET);
    }

    public static void success(String message) {
        System.out.println(ConsoleColors.GREEN + "[OK]    " + now() + " - " + message + ConsoleColors.RESET);
    }

    public static void warn(String message) {
        System.out.println(ConsoleColors.YELLOW + "[WARN]  " + now() + " - " + message + ConsoleColors.RESET);
    }

    public static void error(String message) {
        System.err.println(ConsoleColors.RED + "[ERROR] " + now() + " - " + message + ConsoleColors.RESET);
    }

    public static void error(String message, Throwable t) {
        System.err.println(ConsoleColors.RED + "[ERROR] " + now() + " - " + message
                + " | Cause: " + t.getMessage() + ConsoleColors.RESET);
    }

    private static String now() {
        return LocalDateTime.now().format(DT_FMT);
    }
}
