package org.vorin.bestwords.loaders;

import com.google.common.base.Stopwatch;
import org.apache.commons.io.IOUtils;
import org.vorin.bestwords.util.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.List;

import static java.lang.String.format;
import static org.vorin.bestwords.AppConfig.CACHES_DIR;
import static org.vorin.bestwords.util.Util.sleep;

public class TranslationLoader {

    private static final Logger LOG = Logger.get(TranslationLoader.class);

    private static final long WAIT_BETWEEN_REQUESTS_MS = 5000;

    private final TranslationDataDownloader translationDataDownloader;
    private final TranslationDataParser translationDataParser;
    private final TranslationPublisher translationPublisher;
    private final String source;
    private final boolean useCache;

    private Stopwatch requestStopwatch;

    public TranslationLoader(TranslationDataDownloader translationDataDownloader,
                             TranslationDataParser translationDataParser,
                             TranslationPublisher translationPublisher,
                             String source,
                             boolean useCache) {
        this.translationDataDownloader = translationDataDownloader;
        this.translationDataParser = translationDataParser;
        this.translationPublisher = translationPublisher;
        this.source = source;
        this.useCache = useCache;
    }

    public void load(List<String> words) throws IOException {
        LOG.info(format("source [%s] - loading has started...", source));
        requestStopwatch = Stopwatch.createUnstarted();
        var addedForeignWords = new HashSet<String>();
        for (String word : words) {
            if (addedForeignWords.contains(word)) {
                throw new RuntimeException(format("source [%s] - foreignWord [%s] has been already added - there are some duplicated words it seems", source, word));
            }

            translationDataParser.parseAndPublish(word, getDataForWord(word), translationPublisher);

            addedForeignWords.add(word);
        }
        LOG.info(format("source [%s] - loading complete", source));
    }

    private InputStream getDataForWord(String word) throws IOException {
        if (useCache) {
            File cacheFile = getCacheFileForWord(word);
            if (cacheFile.exists()) {
                LOG.info(format("source [%s] - using the cached file for word=%s", source, word));
                return new FileInputStream(cacheFile);
            }
            LOG.info(format("source [%s] - no cached file for word=%s - downloading content...", source, word));
        }

        while (requestStopwatch.isRunning() && requestStopwatch.elapsed().toMillis() < WAIT_BETWEEN_REQUESTS_MS) {
            sleep(WAIT_BETWEEN_REQUESTS_MS / 50);
        }

        InputStream translationData = translationDataDownloader.download(word);

        requestStopwatch.reset().start();

        if (useCache) {
            File cacheFile = saveTranslationDataToCacheFile(word, translationData);
            return new FileInputStream(cacheFile);
        }
        else {
            return translationData;
        }
    }

    private File saveTranslationDataToCacheFile(String word, InputStream data) throws IOException {
        File cacheFile = getCacheFileForWord(word);
        try(OutputStream fos = new FileOutputStream(cacheFile)){
            IOUtils.copy(data, fos);
        }
        data.close();

        return cacheFile;
    }

    private File getCacheFileForWord(String word) {
        return new File(CACHES_DIR + source + "-cache/" + word);
    }

}
