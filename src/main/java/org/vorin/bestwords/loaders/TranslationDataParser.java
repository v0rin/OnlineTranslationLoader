package org.vorin.bestwords.loaders;

import java.io.IOException;
import java.io.InputStream;

public interface TranslationDataParser {
    void parseAndPublish(WordInfo wordInfo,
                         InputStream translationData,
                         TranslationPublisher translationPublisher) throws IOException;

    String getSource();
}
