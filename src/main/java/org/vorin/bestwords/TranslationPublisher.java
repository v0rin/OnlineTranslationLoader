package org.vorin.bestwords;

import java.io.IOException;

public interface TranslationPublisher {
    void addTranslation(String foreignWord, String meaning, String exampleForeignSentence, String exampleTranslatedSentence) throws IOException;
    void write() throws IOException;
}