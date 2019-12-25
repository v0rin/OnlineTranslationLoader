package org.vorin.bestwords;

import java.io.InputStream;

public interface TranslationDataDownloader {

    InputStream download(String word);
}
