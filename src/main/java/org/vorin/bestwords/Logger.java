package org.vorin.bestwords;

import static java.lang.String.format;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Logger {

    private String tag;

    private static Map<String, Logger> loggers = new ConcurrentHashMap<>();
    private static int LOG_TAG_PADDING = 16;

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
        System.out.println(format("| %" + LOG_TAG_PADDING + "s | INFO | - %s", tag, s));
    }
}