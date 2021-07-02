package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.WordList;

import java.io.IOException;

public interface TranslationPublisher {

    void addMeaning(String foreignWord, String meaning, String source);

    void addMeaning(String foreignWord, String meaning, String wordType, String source);

    void addMeaning(String foreignWord, String meaning, String wordType, String source, String comment);

    void addExampleSentence(String foreignWord, String wordMeaning, String exampleSentence, String source);

    boolean exampleSentenceExists(String foreignWord, String wordMeaning);

    WordList getWordList();

    void writeToTarget() throws IOException;

}