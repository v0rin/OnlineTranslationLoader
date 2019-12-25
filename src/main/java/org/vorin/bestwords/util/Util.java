package org.vorin.bestwords.util;

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

    public static List<String> getForeignWordsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations().stream().map(Translation::getForeignWord).distinct().collect(toList());
    }
}