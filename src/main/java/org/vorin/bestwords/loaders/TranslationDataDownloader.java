package org.vorin.bestwords.loaders;

import java.io.IOException;
import java.io.InputStream;

public interface TranslationDataDownloader {

    InputStream download(String word) throws IOException;
}
