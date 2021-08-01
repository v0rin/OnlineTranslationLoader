package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.CacheFileNameProvider;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.model.Dictionary;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.AppConfig.CACHES_DIR;

public class SynonymStore implements TranslationPublisher {

    private static final Logger LOG = Logger.get(SynonymStore.class);

    private final Map<String, Set<String>> synonymMap = new HashMap<>();
    private final Dictionary dictionary;
    private final TranslationDataParser parser;

    private final CacheFileNameProvider cacheFileNameProvider = new CacheFileNameProvider();

    public SynonymStore(Dictionary dictionary, TranslationDataParser parser) {
        this.dictionary = dictionary;
        this.parser = parser;
    }

    public Set<String> getSynonyms(String word) {
        var synonyms = synonymMap.get(word);
        if (synonyms != null) {
            return synonyms;
        }
        else {
            try {
                parser.parseAndPublish(new WordInfo(word, null),
                                       new FileInputStream(CACHES_DIR + parser.getSource() + "-cache-" +
                                                           dictionary + "/" + cacheFileNameProvider.getFileName(word)),
                                       this);
                return synonymMap.get(word) == null ? new HashSet<String>() : synonymMap.get(word);
            }
            catch (IOException e) {
                return new HashSet<>();
                // throw new RuntimeException(e);
            }
        }
    }

    public List<List<String>> groupBySynonyms(List<String> words) {
        Set<Set<String>> groupSet = new HashSet<>();
        List<List<String>> groups = new ArrayList<>();
        for (var word : words) {
            var group = findSynonymsInList(word, words);
            if (groupSet.add(new HashSet<>(group))) {
                groups.add(group);
            }
        }
        return groups;
    }

    public List<String> findSynonymsInList(String word, List<String> words) {
        Set<String> synonymSet = new HashSet<>();
        List<String> synonyms = new ArrayList<>();
        synonymSet.add(word);
        synonyms.add(word);
        var synonymsForWord = getSynonyms(word);
        for (String w : words) {
            var synonymsForWord2 = getSynonyms(w);
            if (synonymsForWord.contains(w) || synonymsForWord2.contains(word)) {
                if (synonymSet.add(w)) {
                    synonyms.add(w);
                }
            }
        }
        return synonyms;
    }

    public List<String> removeSynonymsFromList(List<String> words) {
        var discardedSynonyms = new HashSet<String>();
        var wordsWithFoundSynonyms = new HashSet<String>();
        for (String word : words) { // word that can have synonyms
            var synonyms = getSynonyms(word);
            for (String word2 : words) { // word that can be a synonym
                if (word.equals(word2)) {
                    continue;
                }

                if (synonyms.contains(word2) && !wordsWithFoundSynonyms.contains(word2)) {
                    if (!discardedSynonyms.contains(word2)) {
                        LOG.info(String.format("remove %s as a synonym of %s", word2, word));
                    }
                    discardedSynonyms.add(word2);
                    wordsWithFoundSynonyms.add(word);
                }
            }
        }

        var newList = words.stream().filter(not(discardedSynonyms::contains)).collect(toList());

        return newList;
    }

    @Override
    public void addMeaning(String foreignWord, String meaning, String source) {
        synonymMap.compute(foreignWord, (k, v) -> {
           if (v == null) {
               v = new HashSet<>();
           }
           v.add(meaning);
           return v;
        });
    }

    @Override
    public void addMeaning(String foreignWord, String meaning, String source, String comment) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void addMeaning(String foreignWord, String meaning, String wordType, String source, String comment) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void addExampleSentence(String foreignWord, String wordMeaning, String exampleSentence, String source) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public boolean exampleSentenceExists(String foreignWord, String wordMeaning) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Wordlist getWordlist() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void writeToTarget() throws IOException {
        throw new UnsupportedOperationException("");
    }
}
