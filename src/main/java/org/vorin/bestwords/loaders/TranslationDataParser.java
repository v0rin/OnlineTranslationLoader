package org.vorin.bestwords;

import java.io.IOException;
import java.io.InputStream;

public interface TranslationDataParser {
    void parse(InputStream translationData) throws IOException;
}
