package org.vorin.bestwords.util;

import org.vorin.bestwords.loaders.WordInfo;
import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.WordList;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
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

    public static List<WordInfo> getReverseForeignWordsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations().stream().flatMap(t -> t.getMeanings().stream().map(m -> new WordInfo(m.getWordMeaning(),  null))).distinct().collect(toList());
    }

    public static List<WordInfo> getForeignWordsWithMeaningsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations()
                .stream()
                .flatMap(t -> t.getMeanings().stream()
                                             .map(m -> new WordInfo(t.getForeignWord(), m.getWordMeaning())))
                .distinct().collect(toList());
    }

    public static List<WordInfo> getReverseForeignWordsWithMeaningsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations()
                .stream()
                .flatMap(t -> t.getMeanings().stream()
                        .map(m -> new WordInfo(m.getWordMeaning(), t.getForeignWord())))
                .distinct().collect(toList());
    }

    public static String chooseShortestString(List<String> strings) {
        return chooseShortestString(strings, 0);
    }

    public static String chooseShortestString(List<String> strings, int preferablyNotShorterThan) {
        checkArgument(strings != null && !strings.isEmpty());

        String shortestWithinLimit = null;
        String shortest = strings.get(0);
        for (String s : strings) {
            if (s.length() < shortest.length()) {
                shortest = s;
            }
            if (shortestWithinLimit == null && s.length() >= preferablyNotShorterThan) {
                shortestWithinLimit = s;
            }
            else if (shortestWithinLimit != null && s.length() >= preferablyNotShorterThan && s.length() < shortestWithinLimit.length()) {
                shortestWithinLimit = s;
            }
        }

        if (shortestWithinLimit != null) {
            return shortestWithinLimit;
        }
        else {
            return shortest;
        }
    }
}