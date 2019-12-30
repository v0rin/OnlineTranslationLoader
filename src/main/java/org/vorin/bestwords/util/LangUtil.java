package org.vorin.bestwords.util;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;

public class LangUtil {

    public static String reverseExampleSentence(String sentence) {
        String[] split = sentence.split(" - ");
        return split[1] + " - " + split[0];
    }

    public static String getParsedForeignWord(String word) {
        int equalCharIdx = word.indexOf("=");
        int slashIdx = word.indexOf("/");
        int bracketIdx = word.indexOf("(");

        int minIdx = Stream.of(word.indexOf("="),
                               word.indexOf("/"),
                               word.indexOf("(")).filter(i -> i > -1).min(Integer::compareTo).orElse(-1);

        if (minIdx > 0) {
            return word.substring(0, minIdx).trim();
        }
        else {
            return word;
        }
    }

    /**
     * 1. if matches the below remove
     * -
     *
     * 2. if the first word followd by one of the below take just the first word:
     * a algo
     * algo a
     * a alguien
     * alguien a
     * a
     * algo
     * alguien
     *
     * 3. if 2 words and ending with the below remove the ending
     * que
     * con
     * en
     * de
     *
     * 4. if more than 3 words remove unless matches the below:
     * word de word
     * word la word
     * word el word
     *
     */
    public static String santizeSpanishMeaning(String meaning) {
        meaning = meaning.trim();

        // 1.
        if (meaning.equals("-")) {
            return "";
        }

        // 2.
        int spaceIdx = meaning.indexOf(" ");
        String[] stringsToMatch = {" a algo", " algo a", " a alguien", " alguien a", " a", " algo", " alguien"};

        for (String match : stringsToMatch) {
            int idx = meaning.indexOf(match);
            if (idx > 0 && idx == spaceIdx) {
                return meaning.substring(0, idx);
            }
        }

        int wordCount = wordCount(meaning);

        // 3.
        String[] stringsToMatch2 = {" que", " con", " de", " en"};
        if (wordCount == 2) {
            for (String match : stringsToMatch2) {
                if (meaning.substring(meaning.length() - match.length()).equals(match)) {
                    return meaning.substring(0, meaning.length() - match.length());
                }
            }
        }

        // 4.
        String[] stringsToMatch3 = {" de ", " la ", " el "};
        spaceIdx = meaning.indexOf(" ");
        if (wordCount == 3) {
            for (String match : stringsToMatch3) {
                int idx = meaning.indexOf(match);
                if (idx > 0 && idx == spaceIdx) {
                    return meaning;
                }
            }
        }
        if (wordCount > 2) {
            return "";
        }


        return meaning;
    }


    public static int wordCount(String meaning) {
        if (isNullOrEmpty(meaning)) {
            return 0;
        }
        int spaceCount = 0;
        String meaningPart = meaning;

        int spaceIdx = meaningPart.indexOf(" ");
        while (spaceIdx > 0) {
            meaningPart = meaningPart.substring(spaceIdx + 1);
            spaceIdx = meaningPart.indexOf(" ");
            spaceCount++;
        }
        return spaceCount + 1;
    }


    public static int wordCount2(String s){

        int wordCount = 0;

        boolean word = false;
        int endOfLine = s.length() - 1;

        for (int i = 0; i < s.length(); i++) {
            // if the char is a letter, word = true.
            if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
                word = true;
                // if char isn't a letter and there have been letters before,
                // counter goes up.
            } else if (!Character.isLetter(s.charAt(i)) && word) {
                wordCount++;
                word = false;
                // last word of String; if it doesn't end with a non letter, it
                // wouldn't count without this.
            } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
                wordCount++;
            }
        }
        return wordCount;
    }


    public static String hexStringToUtf8String(String hexString) {
        return new String(hexStringToByteArray(hexString), StandardCharsets.UTF_8);
    }


    public static byte[] hexStringToByteArray(String s) {
        s = s.replaceAll("\\s", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

}
