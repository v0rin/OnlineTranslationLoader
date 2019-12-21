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

}