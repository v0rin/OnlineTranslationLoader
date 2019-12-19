package org.vorin.bestwords;

public class Util {

    public static void print(String s) {
        System.out.println(s);
    }

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