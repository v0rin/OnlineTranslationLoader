package org.vorin.bestwords.util;

public class Util {

    public static void sleep(long ms) {
        try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
            Thread.currentThread().interrupt();
		}
    }

    public static String stripSurroundingQuotes(String s) {
        return s.replaceAll("^\"|\"$", "");
    }

    public static String trimAndStripTrailingDot(String s) {
        s = s.strip();
        if (s.substring(s.length() - 1).equals(".")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }
}