package org.vorin.bestwords.util;

import org.vorin.bestwords.loaders.WordInfo;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.WordList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

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

    public static List<WordInfo> getForeignWordsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations().stream().map(t -> new WordInfo(t.getForeignWord(), null)).distinct().collect(toList());
    }

    public static String chooseShortestString(List<String> strings) {
        String shortest = null;
        int minLength = Integer.MAX_VALUE;
        for (String s : strings) {
            if (s.length() < minLength) {
                shortest = s;
                minLength = s.length();
            }
        }
        return shortest;
    }
}