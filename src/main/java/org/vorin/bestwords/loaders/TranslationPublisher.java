package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;

import java.io.IOException;

public interface TranslationPublisher {

    void addMeaning(String foreignWord,
                    String meaning,
                    String source);

    void addExampleSentence(String foreignWord,
                            String wordMeaning,
                            String exampleSentence,
                            String source);

    WordList getWordList();

    void writeToTarget() throws IOException;

}