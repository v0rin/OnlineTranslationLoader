package org.vorin.bestwords.util;

import static java.lang.String.format;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * I created the class coz there were some issues with log4j when working on che.openshift.io
 * couldn't find the configuration file or something (and I had to use it when working from BNP Paribas)
 */
public class Logger {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private static final PrintStream CONSOLE_OUT;

    private static int LOG_TAG_PADDING = 16;

    static {
//        CONSOLE_OUT = System.out;
        CONSOLE_OUT = new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    private final String tag;

    public static Logger get(Class<?> clazz) {
        return get(clazz.getSimpleName());
    }

    public static Logger get(String tag) {
        return loggers.computeIfAbsent(tag, t -> {
            LOG_TAG_PADDING = Math.max(LOG_TAG_PADDING, tag.length());
            return new Logger(t);
        });
    }

    private Logger(String tag) {
        this.tag = tag;
    }

    public void info(String s) {
        log(s, "INFO");
    }

    public void warn(String s) {
        log(s, "WARN");
    }

    public void error(String s) {
        log(s, "ERROR");
    }

    private void log(String s, String lvl) {
        CONSOLE_OUT.println(format("%s | %" + LOG_TAG_PADDING + "s | %5s | - %s",
                DATE_TIME_FORMATTER.format(LocalTime.now()), tag, lvl, s));
    }

}