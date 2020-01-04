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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class WordListProcessor {

    private static final Logger LOG = Logger.get(WordListProcessor.class);
    private static final Pattern WEIRD_CHARACTERS_PATTERN = Pattern.compile("[\\(\\)\\;\\:\\,\\.\\@\\#\\$\\%\\^\\&\\*\\|\\{\\}\\\"\\'\\/\\<\\>\\~`]+");
    private static final String LOG_TRANSLATION_COMMENT_FORMAT = "foreignWord=[%s] - %s";
    private static final String LOG_MEANING_COMMENT_FORMAT = "foreignWord=[%s], meaning=[%s] - %s";

    private final SynonymStore SYNONYMS;
    private final Set<String> MOST_COMMON_WORDS;
    private final WordList GOOGLE_WORDLIST;
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
            this.GOOGLE_WORDLIST = WordList.loadFromXml( getWordListFile("GoogleTranslateWordList.xml"));
            this.GOOGLE_REVERSE_WORDLIST = WordList.loadFromXml( getWordListFile("GoogleTranslateReverseWordList.xml"));
            this.LINGUEE_WORDLIST = WordList.loadFromXml( getWordListFile("LingueeWordList.xml"));
            this.WORD_REFERENCE_WORDLIST = WordList.loadFromXml( getWordListFile("WordReferenceWordList.xml"));
//            this.COLLINS_REVERSE_WORDLIST = WordList.loadFromXml( getWordListFile("CollinsReverseWordList.xml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void combineMeanings(Translation targetTranslation) {
        var alreadyAddedMeanings = new HashSet<String>();
        addMeaningsFromWordList(targetTranslation, GOOGLE_WORDLIST, alreadyAddedMeanings);
        addMeaningsFromWordList(targetTranslation, WORD_REFERENCE_WORDLIST, alreadyAddedMeanings);
        addMeaningsFromWordList(targetTranslation, LINGUEE_WORDLIST, alreadyAddedMeanings);
    }

    private void addMeaningsFromWordList(Translation targetTranslation, WordList w, Set<String> alreadyAddedMeanings) {
        var t = w.findTranslationForWord(targetTranslation.getForeignWord());
        if (t != null) {
            for (var meaning : t.getMeanings()) {
                if (!alreadyAddedMeanings.contains(meaning.getWordMeaning())) {
                    targetTranslation.getMeanings().add(meaning);
                    alreadyAddedMeanings.add(meaning.getWordMeaning());
                }
            }
        }
    }

    public boolean processTranslation(Translation t) {
        boolean problemsExist = false;
        var meanings = t.getMeanings();

        String sanitizedForeignWord = LangUtil.sanitizeWord(t.getForeignWord());
        for (var iter = meanings.iterator(); iter.hasNext();) {
            var m = iter.next();

            String sanitizedMeaning = LangUtil.sanitizeWord(m.getWordMeaning());

            if (LangUtil.wordCount(sanitizedMeaning) > 1) {
                addMeaningComment(t.getForeignWord(), m, "meaning has multiple words");
                problemsExist = true;
                iter.remove();
                continue;
            }

//            setExampleSentence(t.getForeignWord(), m);

            int sourcesCount = 1;
            int mostCommonWords = 1;
            if (LangUtil.wordCount(sanitizedMeaning) == 1 && !MOST_COMMON_WORDS.contains(sanitizedMeaning)) {
                addMeaningComment(t.getForeignWord(), m, "not in MOST_COMMON_WORDS");
                mostCommonWords = 0;
            }
            sourcesCount++;
            int googleWordList = 1;
            if (!existsInWordlist(GOOGLE_WORDLIST, sanitizedForeignWord, sanitizedMeaning, "GOOGLE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in GOOGLE_WORDLIST");
                googleWordList = 0;
            }
            sourcesCount++;
            int googleReverseWordList = 1;
            if (!existsInReverseWordlist(GOOGLE_REVERSE_WORDLIST, sanitizedForeignWord, sanitizedMeaning, "GOOGLE_REVERSE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in GOOGLE_REVERSE_WORDLIST");
                googleReverseWordList = 0;
            }
            sourcesCount++;
            int wordReferenceWordList = 1;
            if (!existsInWordlist(WORD_REFERENCE_WORDLIST, sanitizedForeignWord, sanitizedMeaning, "WORD_REFERENCE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in WORD_REFERENCE_WORDLIST");
                wordReferenceWordList = 0;
            }
            sourcesCount++;
            int lingueeWordList = 1;
            if (!existsInWordlist(LINGUEE_WORDLIST, sanitizedForeignWord, sanitizedMeaning, "LINGUEE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in LINGUEE_WORDLIST");
                lingueeWordList = 0;
            }

            int inCount = mostCommonWords + googleWordList + googleReverseWordList + wordReferenceWordList + lingueeWordList;
            problemsExist = problemsExist || inCount < sourcesCount;

            // google and common
            // !google and common and at least 2 other sources
            // !common and google and at least 2 other sources
            if (mostCommonWords == 1 && googleWordList == 1) {
            }
            else if (googleWordList == 0 && mostCommonWords == 1 && inCount >= 2) {
            }
            else if (googleWordList == 1 && mostCommonWords == 0 && inCount >= 2) {
            }
            else {
                iter.remove();
                continue;
            }
//            int mustBeInAtLeastNSources = 3;
//            if (inCount < mustBeInAtLeastNSources) {
//                addMeaningComment(t.getForeignWord(), m, format("not in at least %s sources [%s]", mustBeInAtLeastNSources, inCount));
//            }

            Matcher matcher = WEIRD_CHARACTERS_PATTERN.matcher(sanitizedMeaning);
            if (matcher.find()) {
                addMeaningComment(t.getForeignWord(), m, "meaning has weird characters");
                problemsExist = true;
            }

        }

        var wordMeanings = t.getMeanings().stream().map(Meaning::getWordMeaning).collect(Collectors.toList());
        wordMeanings = SYNONYMS.removeSynonyms(wordMeanings);
        for (var iter = meanings.iterator(); iter.hasNext();) {
            var m = iter.next();
            String sanitizedMeaning = LangUtil.sanitizeWord(m.getWordMeaning());
            if (!wordMeanings.contains(sanitizedMeaning)) {
                addMeaningComment(t.getForeignWord(), m, "is a synonym");
            }
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
