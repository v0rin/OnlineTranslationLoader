package org.vorin.bestwords;

public interface TranslationPublisher {
    void addTranslation(String foreignWord, String meaning, String exampleForeignSentence, String exampleTranslatedSentence);
}