package org.vorin.bestwords.loaders;

import org.vorin.bestwords.model.Wordlist;

import java.io.IOException;

public interface TranslationPublisher {

    void addMeaning(String foreignWord, String meaning, String source);

    void addMeaning(String foreignWord, String meaning, String wordType, String source);

    void addMeaning(String foreignWord, String meaning, String wordType, String source, String comment);

    void addExampleSentence(String foreignWord, String wordMeaning, String exampleSentence, String source);

    boolean exampleSentenceExists(String foreignWord, String wordMeaning);

    Wordlist getWordlist();

    void writeToTarget() throws IOException;

}