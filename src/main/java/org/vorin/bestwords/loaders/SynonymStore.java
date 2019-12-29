package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SynonymStore implements TranslationPublisher {

    private final Map<String, Set<String>> synonyms = new HashMap<>();

    public Set<String> getSynonyms(String word) {
        return synonyms.get(word);
    }


    @Override
    public void addMeaning(String foreignWord, String meaning, String source) {
        synonyms.compute(foreignWord, (k, v) -> {
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
