package org.vorin.bestwords;

import java.io.IOException;

public interface TranslationPublisher {

    void addMeaning(String foreignWord,
                    String meaning,
                    String source);

    void addExampleSentence(String foreignWord,
                            String wordMeaning,
                            String exampleSentence,
                            String source);

    void writeToTarget() throws IOException;

}