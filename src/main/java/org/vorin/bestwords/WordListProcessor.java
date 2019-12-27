package org.vorin.bestwords;

import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.LangUtil;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class WordListProcessor {

/**
 * @meanings:
 * - nie ma znaczen
 * - cannot be more than 3 meanings - it is the first 1000 words - don't make it too complicated
 * - two or more words
 * - search for () and other weird characters
 * - has to be in SpanishCombined2954.txt (it is Spanish1kNeri i Spanish3kAndki2134488481)
 * - liczby - powinny miec tylko jedno znaczenie - poza one
 * - check if google reverse gives the original meaning as the main one
 * - cross check if it appears in 2 other dictionaries - possibly also reverse check, we'll see how it works
 *
 * - often should be just one meaning - I would assume at least 2/3
 */

    private static final Logger LOG = Logger.get(WordListProcessor.class);
    private static final Pattern WEIRD_CHARACTERS_PATTERN = Pattern.compile("[\\(\\)\\;\\:\\,\\.\\@\\#\\$\\%\\^\\&\\*\\|\\{\\}\\\"\\'\\/\\<\\>\\~`]+");
    private static final String LOG_TRANSLATION_COMMENT_FORMAT = "foreignWord=[%s] - %s";
    private static final String LOG_MEANING_COMMENT_FORMAT = "foreignWord=[%s], meaning=[%s] - %s";

    private static final Set<String> SPANISH_3K_WORDS;
    private static final WordList GOOGLE_REVERSE_WORDLIST;
    private static final WordList LINGUEE_WORDLIST;
    private static final WordList WORD_REFERENCE_WORDLIST;
    private static final WordList COLLINS_REVERSE_WORDLIST;

    static {
        try {
            SPANISH_3K_WORDS = Util.loadWordFromTxtFile(new File(AppConfig.RES_DIR + "SpanishCombined2954_justwords.txt"));
            GOOGLE_REVERSE_WORDLIST = WordList.loadFromXml(new File(AppConfig.RES_DIR + "googleTranslateReverseWordList.xml"));
            LINGUEE_WORDLIST = WordList.loadFromXml(new File(AppConfig.RES_DIR + "lingueeWordList.xml"));
            WORD_REFERENCE_WORDLIST = WordList.loadFromXml(new File(AppConfig.RES_DIR + "wordReferenceWordList.xml"));
            COLLINS_REVERSE_WORDLIST = WordList.loadFromXml(new File(AppConfig.RES_DIR + "collinsReverseWordList.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean processTranslation(Translation t) {
        boolean problemsExist = false;
        var meanings = t.getMeanings();
        if (meanings.isEmpty()) {
            addTranslationComment(t, "no meanings");
            problemsExist = true;
        }

        if (Arrays.asList("zero", "two", "three", "four", "five", "six", "seven",
                          "eight", "nine", "ten", "twenty", "hundred", "thousand", "million")
                .contains(t.getForeignWord()) && meanings.size() > 1) {
            addTranslationComment(t, "numbers should have only 1 meaning");
            problemsExist = true;
        }

        if (meanings.size() > 3) {
            addTranslationComment(t, "more than 3 meanings");
            problemsExist = true;
        }

        for (var m : meanings) {
            Matcher matcher = WEIRD_CHARACTERS_PATTERN.matcher(m.getWordMeaning());
            if (matcher.find()) {
                addMeaningComment(t.getForeignWord(), m, "means has weird characters");
                problemsExist = true;
            }
            if (LangUtil.wordCount(m.getWordMeaning()) == 1 && !SPANISH_3K_WORDS.contains(m.getWordMeaning())) {
                addMeaningComment(t.getForeignWord(), m, "not in spanish3kWords");
                problemsExist = true;
            }
            if (!existsInReverseWordlist(GOOGLE_REVERSE_WORDLIST, t.getForeignWord(), m.getWordMeaning(), "GOOGLE_REVERSE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "does not exist in GOOGLE_REVERSE_WORDLIST");
                problemsExist = true;
            }
            if (!existsInWordlist(WORD_REFERENCE_WORDLIST, LangUtil.getParsedForeignWord(t.getForeignWord()), m.getWordMeaning(), "WORD_REFERENCE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "does not exist in WORD_REFERENCE_WORDLIST");
                problemsExist = true;
            }
            if (!existsInWordlist(LINGUEE_WORDLIST, LangUtil.getParsedForeignWord(t.getForeignWord()), m.getWordMeaning(), "LINGUEE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "does not exist in LINGUEE_WORDLIST");
                problemsExist = true;
            }
            if (LangUtil.wordCount(m.getWordMeaning()) > 1) {
                addMeaningComment(t.getForeignWord(), m, "meaning has multiple words");
                problemsExist = true;
            }
        }

        return !problemsExist;
    }

    private boolean existsInWordlist(WordList wordList, String foreignWord, String wordMeaning, String wordListName) {
        var t = wordList.findTranslationForWord(foreignWord);
        if (t == null) {
            LOG.error(format("could not find translation for word [%s] in %s", wordMeaning, wordListName));
            return false;
        }

        if (wordList.findMeaning(foreignWord, wordMeaning) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean existsInReverseWordlist(WordList wordList, String foreignWord, String wordMeaning, String wordListName) {
        var t = wordList.findTranslationForWord(wordMeaning);
        if (t == null) {
            LOG.error(format("could not find translation for word [%s] in %s", wordMeaning, wordListName));
            return false;
        }

        if (wordList.findMeaning(wordMeaning, foreignWord) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    public void verifyWordList(WordList w) {
        // check meaning distribution
        var meaningCountDistribution = new HashMap<Integer, Integer>();
        for (var t : w.getTranslations()) {
            meaningCountDistribution.compute(t.getMeanings().size(), (k, v) -> v == null ? 1 : v + 1);
        }
        LOG.info("Distribution=" + meaningCountDistribution);
    }


    private void addTranslationComment(Translation t, String comment) {
        LOG.info(format(LOG_TRANSLATION_COMMENT_FORMAT, t.getForeignWord(), comment));
        t.addComment(comment);
    }


    private void addMeaningComment(String foreignWord, Meaning m, String comment) {
        LOG.info(format(LOG_MEANING_COMMENT_FORMAT, foreignWord, m.getWordMeaning(), comment));
        m.addComment(comment);
    }

}
