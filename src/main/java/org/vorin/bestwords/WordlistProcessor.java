package org.vorin.bestwords;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vorin.bestwords.loaders.SynonymStore;
import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.*;
import org.vorin.bestwords.model.Dictionary;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.util.Sources.COLLINS_SOURCE;

/**
 * to jest taka troche smieciowa klasa, w ktorej jest iles elementow robionych recznie
 */
public class WordlistProcessor {

    private static final Logger LOG = Logger.get(WordlistProcessor.class);
    private static final Pattern WEIRD_CHARACTERS_PATTERN = Pattern.compile("[\\(\\)\\;\\:\\,\\.\\@\\#\\$\\%\\^\\&\\*\\|\\{\\}\\\"\\'\\/\\<\\>\\~`]+");
    private static final String LOG_TRANSLATION_COMMENT_FORMAT = "foreignWord=[%s] - %s";
    private static final String LOG_MEANING_COMMENT_FORMAT = "foreignWord=[%s], meaning=[%s] - %s";

    private final SynonymStore synonyms;
    private final Set<String> mostCommonWords;
    private final Wordlist googleWordlist;
    private final Wordlist googleReverseWordlist;
    private final Wordlist lingueeWordlist;
    private final Wordlist wordReferenceWordlist;

    private Wordlist collinsReverseWordlist;

    private final Dictionary dictionary;


    /**
    * Once I generated translations from diff sources (google, linguee, ..) I try to combine the results
    * But the lists mostCommonWords, googleWordlist, ... need to already exist
    */
    public WordlistProcessor(Dictionary dictionary, SynonymStore synonymStore) {
        this.dictionary = dictionary;
        try {
            this.synonyms = synonymStore;
            this.mostCommonWords = Util.loadWordsFromTxtFile( getWordlistFile("MostCommonWords.txt")); // to verify if the meaning exists in the most common words
            this.googleWordlist = Wordlist.loadFromXml( getWordlistFile("GoogleTranslateWordlist.xml"));
            this.googleReverseWordlist = Wordlist.loadFromXml( getWordlistFile("GoogleTranslateReverseWordlist.xml"));
            this.lingueeWordlist = Wordlist.loadFromXml( getWordlistFile("LingueeWordlist.xml"));
            this.wordReferenceWordlist = Wordlist.loadFromXml( getWordlistFile("WordReferenceWordlist.xml"));
            if (dictionary == Dictionary.EN_ES) {
                this.collinsReverseWordlist = Wordlist.loadFromXml( getWordlistFile("CollinsReverseWordlist.xml"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static void combineMeanings(Translation targetTranslation, Map<String, Wordlist> wordlists) {
        // collect all meanings
        List<String> allWordMeanings = new ArrayList<>();
        Map<String, List<String>> meaningLists = new HashMap<>();
        for (var entry : wordlists.entrySet()) {
            var source = entry.getKey();
            var wordlist = entry.getValue();
            var meaningList = getWordMeaningsFromWordlist(targetTranslation, wordlist);
            meaningLists.put(source, meaningList);
            allWordMeanings.addAll(meaningList);
        }
        allWordMeanings = allWordMeanings.stream().distinct().collect(toList());

        var wordMeaningMap = new HashMap<String, List<Pair<String, String>>>();
        // add in which sources the wordMeaning exists
        for (String wordMeaning : allWordMeanings) {
            for (String source : wordlists.keySet()) {
                if (meaningLists.get(source).contains(wordMeaning)) {
                    var m = wordlists.get(source).findMeaning(targetTranslation.getForeignWord(), wordMeaning);
                    addSource(wordMeaningMap, wordMeaning, m.getExampleSentence(), source);
                }
            }
        }

        // sort by in how many sources a wordMeaning exists
        List<String> sortedWordMeanings = allWordMeanings
                .stream()
                .sorted(Comparator.comparing(m -> wordMeaningMap.get(m).size()).reversed())
                .collect(toList());

        for (var wordMeaning : sortedWordMeanings) {
            String wordMeaningSources = wordMeaningMap.get(wordMeaning).stream().map(Pair::getRight).collect(joining(", "));
            String exampleSentences = wordMeaningMap.get(wordMeaning).stream()
                    .filter(p -> !p.getLeft().isBlank())
                    .map(Pair::getLeft)
                    .collect(joining(" || "));
            String exampleSentenceSources = wordMeaningMap.get(wordMeaning).stream()
                    .filter(p -> !p.getLeft().isBlank())
                    .map(Pair::getRight)
                    .collect(joining(", "));
            // TODO possibly to do the wordType
            targetTranslation.getMeanings().add(new Meaning(wordMeaning, null, exampleSentences, "", wordMeaningSources, exampleSentenceSources));
        }
    }


    private static void addSource(Map<String, List<Pair<String, String>>> map, String wordMeaning, String exampleSentence, String source) {
        map.compute(wordMeaning, (m, sources) -> {
            if (sources == null) {
                sources = new ArrayList<>();
            }
            sources.add(ImmutablePair.of(exampleSentence, source));
            return sources;
        });
    }


    private static List<String> getWordMeaningsFromWordlist(Translation translation, Wordlist w) {
        var t = w.findTranslationForWord(translation.getForeignWord());
        if (t != null) {
            return t.getMeanings().stream().map(Meaning::getWordMeaning).collect(toList());
        }
        else {
            return new ArrayList<>();
        }
    }


    /**
    * - Processes all the meanings for the translation looking at a set of rules to determine if there are problems with the meanings<br>
    * - Also if there are some problems it just removes the meaning, e.g. if the meaning is only in one of the less important sources<br>
    * - Also groups meanings that are synonyms and take the most important one (I don't remember how? maybe in how many sources exists?) and add the rest in the comment<br>
    */
    public boolean processMeaningsForTranslation(Translation t) {
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

            int sourcesCount = 1;
            int mostCommonWords = 1;
            if (LangUtil.wordCount(sanitizedMeaning) == 1 && !this.mostCommonWords.contains(sanitizedMeaning)) {
                addMeaningComment(t.getForeignWord(), m, "not in MOST_COMMON_WORDS");
                mostCommonWords = 0;
            }
            sourcesCount++;
            int googleWordlist = 1;
            if (!existsInWordlist(this.googleWordlist, sanitizedForeignWord, sanitizedMeaning, "GOOGLE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in GOOGLE_WORDLIST");
                googleWordlist = 0;
            }
//            sourcesCount++;
//            int googleReverseWordlist = 1;
//            if (!existsInReverseWordlist(GOOGLE_REVERSE_WORDLIST, sanitizedForeignWord, sanitizedMeaning, "GOOGLE_REVERSE_WORDLIST")) {
//                addMeaningComment(t.getForeignWord(), m, "not in GOOGLE_REVERSE_WORDLIST");
//                googleReverseWordlist = 0;
//            }
            sourcesCount++;
            int wordReferenceWordlist = 1;
            if (!existsInWordlist(this.wordReferenceWordlist, sanitizedForeignWord, sanitizedMeaning, "WORD_REFERENCE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in WORD_REFERENCE_WORDLIST");
                wordReferenceWordlist = 0;
            }
            sourcesCount++;
            int lingueeWordlist = 1;
            if (!existsInWordlist(this.lingueeWordlist, sanitizedForeignWord, sanitizedMeaning, "LINGUEE_WORDLIST")) {
                addMeaningComment(t.getForeignWord(), m, "not in LINGUEE_WORDLIST");
                lingueeWordlist = 0;
            }

//            int inCount = mostCommonWords + googleWordlist + googleReverseWordlist + wordReferenceWordlist + lingueeWordlist;
            int inCount = googleWordlist + wordReferenceWordlist + lingueeWordlist;
            problemsExist = problemsExist || inCount < sourcesCount;

            if (dictionary == Dictionary.EN_PL) {
                if (mostCommonWords == 0 && inCount == 1) {
                    iter.remove();
                    continue;
                }
            }

            if (dictionary == Dictionary.EN_ES) {
                if (wordReferenceWordlist == 1 && inCount == 1) {
                    iter.remove();
                    continue;
                }
                if (lingueeWordlist == 1 && inCount == 1) {
                    iter.remove();
                    continue;
                }
                if (mostCommonWords == 0 && inCount == 1) {
                    iter.remove();
                    continue;
                }
            }

            Matcher matcher = WEIRD_CHARACTERS_PATTERN.matcher(sanitizedMeaning);
            if (matcher.find()) {
                addMeaningComment(t.getForeignWord(), m, "meaning has weird characters");
                problemsExist = true;
            }

        }

        var wordMeanings = t.getMeanings().stream().map(Meaning::getWordMeaning).collect(toList());
        List<List<String>> synonymGroups = synonyms.groupBySynonyms(wordMeanings);
        var meaningsToRemove = new ArrayList<Meaning>();
        var wordMeaningsToKeep = new HashSet<String>();
        Map<Meaning, List<String>> synonymsForMeanings = new HashMap<>();
        for (var group : synonymGroups) {
            // get the most important meaning - the order of meanings should be according to importance
            var wordMeaning = group.get(0);
            // find it in the meanings and schedule to add the comment with synonyms
            var meaning = Wordlist.findMeaning(t, wordMeaning);
            group.remove(wordMeaning);
            wordMeaningsToKeep.add(wordMeaning);
            if (!group.isEmpty()) {
                synonymsForMeanings.compute(meaning, (m, s) -> {
                    if (s == null) {
                        s = new ArrayList<>();
                    }
                    s.addAll(group);
                    return s;
                });
            }

            // schedule to remove all others
            for (var wordMeaningToRemove : group) {
                if (wordMeaningsToKeep.contains(wordMeaningToRemove)) {
                    continue;
                }
                var meaningToRemove = Wordlist.findMeaning(t, wordMeaningToRemove);
                if (meaningToRemove != null) {
                    meaningsToRemove.add(meaningToRemove);
                }
            }
        }
        for (var entry : synonymsForMeanings.entrySet()) {
            entry.getKey().addComment("possible synonyms - " + entry.getValue().stream().distinct().collect(joining(", ")));
        }
        for (var m : meaningsToRemove) {
            t.getMeanings().remove(m);
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


    public void processExampleSentencesForTranslation(Translation targetTranslation) {
        for (var m : targetTranslation.getMeanings()) {
            List<Pair<String, String>> exampleSentences = parseExampleSentences(m);

            if (collinsReverseWordlist != null) {
                var collinsMeaning = collinsReverseWordlist.findMeaning(m.getWordMeaning(), targetTranslation.getForeignWord());
                if (collinsMeaning != null && !collinsMeaning.getExampleSentence().isBlank()) {
                    exampleSentences.add(ImmutablePair.of(collinsMeaning.getExampleSentence(), COLLINS_SOURCE));
                }
            }

            // go through example sentences and choose the best one
            m.setExampleSentence(exampleSentences.stream().map(Pair::getLeft).collect(joining(" || ")));
            m.setExampleSentenceSource(exampleSentences.stream().map(Pair::getRight).collect(joining(", ")));
        }
    }

    private List<Pair<String, String>> parseExampleSentences(Meaning m) {
        if (m.getExampleSentence().isBlank()) {
            return new ArrayList<>();
        }
        String[] sentences = m.getExampleSentence().split(" \\|\\| ");
        String[] sources = m.getExampleSentenceSource().split(", ");

        if (sentences.length == 0 || sentences.length != sources.length) {
            throw new RuntimeException(format("This should not happen! sentences.length [%s] == 0 || sentences.length [%s] != sources.length [%s]",
                                              sentences.length, sentences.length, sources.length));
        }

        return IntStream.range(0, sentences.length).boxed().map(i -> ImmutablePair.of(sentences[i], sources[i])).collect(toList());
    }


    private boolean existsInWordlist(Wordlist wordlist, String foreignWord, String wordMeaning, String wordlistName) {
        var t = wordlist.findTranslationForWord(foreignWord);
        if (t == null) {
            LOG.error(format("could not find translation for word [%s] in %s", wordMeaning, wordlistName));
            return false;
        }

        if (wordlist.findMeaning(foreignWord, wordMeaning) != null) {
            return true;
        }
        else {
            return false;
        }
    }


    private boolean existsInReverseWordlist(Wordlist wordlist, String foreignWord, String wordMeaning, String wordlistName) {
        var t = wordlist.findTranslationForWord(wordMeaning);
        if (t == null) {
            LOG.error(format("could not find translation for word [%s] in %s", wordMeaning, wordlistName));
            return false;
        }

        if (wordlist.findMeaning(wordMeaning, foreignWord) != null) {
            return true;
        }
        else {
            return false;
        }
    }


    public void verifyWordlist(Wordlist w) {
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


    private File getWordlistFile(String wordlistName) {
        return new File(AppConfig.RES_DIR + dictionary.name() + "-" + wordlistName);
    }

}
