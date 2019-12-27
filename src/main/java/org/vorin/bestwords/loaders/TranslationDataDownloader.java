package org.vorin.bestwords.loaders;

import org.vorin.bestwords.util.Dictionary;

import java.io.IOException;
import java.io.InputStream;

public interface TranslationDataDownloader {

    InputStream download(String word) throws IOException;

    Dictionary getDictionary();
}
