package org.vorin.bestwords;

import org.vorin.bestwords.loaders.SynonymStore;
import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
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
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.vorin.bestwords.util.Sources.*;

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

    private final SynonymStore SYNONYMS;
    private final Set<String> MOST_COMMON_WORDS;
    private final WordList GOOGLE_REVERSE_WORDLIST;
    private final WordList LINGUEE_WORDLIST;
    private final WordList WORD_REFERENCE_WORDLIST;
//    private final WordList COLLINS_REVERSE_WORDLIST;

    private final Dictionary dictionary;

    public WordListProcessor(Dictionary dictionary) {
        this.dictionary = dictionary;
        try {
            this.SYNONYMS = new SynonymStore();
            this.MOST_COMMON_WORDS = Util.loadWordsFromTxtFile( getWordListFile("MostCommonWords.txt"));
            this.GOOGLE_REVERSE_WORDLIST = WordList.loadFromXml( getWordListFile("GoogleTranslateReverseWordList.xml"));
            this.LINGUEE_WORDLIST = WordList.loadFromXml( getWordListFile("LingueeWordList.xml"));
            this.WORD_REFERENCE_WORDLIST = WordList.loadFromXml( getWordListFile("WordReferenceWordList.xml"));
//            this.COLLINS_REVERSE_WORDLIST = WordList.loadFromXml( getWordListFile("CollinsReverseWordList.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean processTranslation(Translation t) {
        boolean problemsExist = false;
        var meanings = t.getMeanings();
        var iter = meanings.iterator();

        var wordMeanings = t.getMeanings().stream().map(Meaning::getWordMeaning).collect(Collectors.toList());
        wordMeanings = SYNONYMS.removeSynonyms(wordMeanings);

        while (iter.hasNext()) {
            var m = iter.next();

            if (!wordMeanings.contains(m.getWordMeaning())) {
                iter.remove();
                continue;
            }

            Matcher matcher = WEIRD_CHARACTERS_PATTERN.matcher(m.getWordMeaning());
            if (matcher.find()) {
                addMeaningComment(t.getForeignWord(), m, "meaning has weird characters, removing...");
//                iter.remove();
                problemsExist = true;
//                continue;
            }

            setExampleSentence(t.getForeignWord(), m);

            int mostCommonWords = 0;
            if (LangUtil.wordCount(m.getWordMeaning()) == 1 && !MOST_COMMON_WORDS.contains(m.getWordMeaning())) {
                addMeaningComment(t.getForeignWord(), m, "not in MOST_COMMON_WORDS");
                mostCommonWords = 1;
            }
            int googleReverseWordList = 0;
            if (!existsInReverseWordlist(GOOGLE_REVERSE_WORDLIST, t.getForeignWord(), m.getWordMeaning(), "GOOGLE_REVERSE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in GOOGLE_REVERSE_WORDLIST");
                googleReverseWordList = 1;
            }
            int wordReferenceWordList = 0;
            if (!existsInWordlist(WORD_REFERENCE_WORDLIST, LangUtil.getParsedForeignWord(t.getForeignWord()), m.getWordMeaning(), "WORD_REFERENCE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in WORD_REFERENCE_WORDLIST");
                wordReferenceWordList = 1;
            }
            int lingueeWordList = 0;
            if (!existsInWordlist(LINGUEE_WORDLIST, LangUtil.getParsedForeignWord(t.getForeignWord()), m.getWordMeaning(), "LINGUEE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in LINGUEE_WORDLIST");
                lingueeWordList = 1;
            }
            boolean multipleWords = false;
            if (LangUtil.wordCount(m.getWordMeaning()) > 1) {
                addMeaningComment(t.getForeignWord(), m, "meaning has multiple words");
                multipleWords = true;
            }

//            if ((mostCommonWords + googleReverseWordList + wordReferenceWordList + lingueeWordList) > 2) {
//                addMeaningComment(t.getForeignWord(), m, "too many problems, removing...");
//                iter.remove();
//            }

//            problemsExist = problemsExist || multipleWords || (mostCommonWords + googleReverseWordList + wordReferenceWordList + lingueeWordList) > 0;
            problemsExist = problemsExist || multipleWords || (googleReverseWordList + wordReferenceWordList + lingueeWordList) > 0;
        }

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

        return !problemsExist;
    }


    private void setExampleSentence(String foreignWord, Meaning meaning) {
//        var collinsMeaning = COLLINS_REVERSE_WORDLIST.findMeaning(meaning.getWordMeaning(), foreignWord);
//        var lingueeMeaning = LINGUEE_WORDLIST.findMeaning(foreignWord, meaning.getWordMeaning());
//        var wrMeaning = WORD_REFERENCE_WORDLIST.findMeaning(foreignWord, meaning.getWordMeaning());
//        if (collinsMeaning != null && !collinsMeaning.getExampleSentence().isEmpty()) {
//            meaning.setExampleSentence(LangUtil.reverseExampleSentence(collinsMeaning.getExampleSentence()));
//            meaning.setExampleSentenceSource(COLLINS_SOURCE);
//        }
//        else if (lingueeMeaning != null && !lingueeMeaning.getExampleSentence().isEmpty()) {
//            meaning.setExampleSentence(lingueeMeaning.getExampleSentence());
//            meaning.setExampleSentenceSource(LINGUEE_SOURCE);
//        }
//        else if (wrMeaning != null && !wrMeaning.getExampleSentence().isEmpty()) {
//            meaning.setExampleSentence(wrMeaning.getExampleSentence());
//            meaning.setExampleSentenceSource(WORD_REFERENCE_SOURCE);
//        }
//        else {
//            addMeaningComment(foreignWord, meaning, "no example sentence");
//        }
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


    private File getWordListFile(String wordListName) {
        return new File(AppConfig.RES_DIR + dictionary.name() + "-" + wordListName);
    }

}
