package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.AppConfig.CACHES_DIR;
import static org.vorin.bestwords.util.Dictionary.EN_PL;
import static org.vorin.bestwords.util.Sources.SYNONIM_NET_SOURCE;

public class SynonymStore implements TranslationPublisher {

    private static final Logger LOG = Logger.get(SynonymStore.class);

    private final Map<String, Set<String>> synonymMap = new HashMap<>();
    private final SynonimNetParser parser = new SynonimNetParser();

    public Set<String> getSynonyms(String word) {
        var synonyms = synonymMap.get(word);
        if (synonyms != null) {
            return synonyms;
        }
        else {
            try {
                parser.parseAndPublish(new WordInfo(word, null),
                                       new FileInputStream(CACHES_DIR + SYNONIM_NET_SOURCE + "-cache-" + EN_PL + "/" + word),
                                       this);
                return synonymMap.get(word) == null ? new HashSet<String>() : synonymMap.get(word);
            }
            catch (IOException e) {
                return new HashSet<String>();
                // throw new RuntimeException(e);
            }
        }
    }

    public List<String> removeSynonyms(List<String> words) {
        var discardedSynonyms = new HashSet<String>();
        for (int i = 0; i < words.size() - 1; i++) { // word that can have synonyms
            String word = words.get(i);
            for (int j = i + 1; j < words.size(); j++) { // word that can be a synonym
                String word2 = words.get(j);
                var synonyms = getSynonyms(word);
                if (synonyms.contains(word2)) {
                    if (!discardedSynonyms.contains(word2)) {
                        LOG.info(String.format("remove %s as a synonym of %s", word2, word));
                    }
                    discardedSynonyms.add(word2);
                    continue;
                }
            }
        }

        var newList = words.stream().filter(not(discardedSynonyms::contains)).collect(toList());

        return newList;
    }

    public List<String> removeSynonyms2(List<String> words) {
        var discardedSynonyms = new HashSet<String>();
        for (String word : words) { // word that can have synonyms
            for (String word2 : words) { // word that can be a synonym
                if (word.equals(word2)) {
                    continue;
                }

                var synonyms = getSynonyms(word);
                if (synonyms.contains(word2)) {
                    if (!discardedSynonyms.contains(word2)) {
                        LOG.info(String.format("remove %s as a synonym of %s", word2, word));
                    }
                    discardedSynonyms.add(word2);
                    continue;
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
    public void addExampleSentence(String foreignWord, String wordMeaning, String exampleSentence, String source) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public boolean exampleSentenceExists(String foreignWord, String wordMeaning) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public WordList getWordList() {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void writeToTarget() throws IOException {
        throw new UnsupportedOperationException("");
    }
}
