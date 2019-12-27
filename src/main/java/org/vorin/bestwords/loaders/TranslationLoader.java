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

    private static final long DEFAULT_WAIT_BETWEEN_REQUESTS_MS = 3000;

    private final TranslationDataDownloader translationDataDownloader;
    private final TranslationDataParser translationDataParser;
    private final TranslationPublisher translationPublisher;
    private final boolean useCache;
    private final long waitBetweenRequestsMs;

    private Stopwatch requestStopwatch;

    public TranslationLoader(TranslationDataDownloader translationDataDownloader,
                             TranslationDataParser translationDataParser,
                             TranslationPublisher translationPublisher,
                             boolean useCache) {
        this(translationDataDownloader, translationDataParser, translationPublisher, useCache, DEFAULT_WAIT_BETWEEN_REQUESTS_MS);
    }

    public TranslationLoader(TranslationDataDownloader translationDataDownloader,
                             TranslationDataParser translationDataParser,
                             TranslationPublisher translationPublisher,
                             boolean useCache,
                             long waitBetweenRequestsMs) {
        this.translationDataDownloader = translationDataDownloader;
        this.translationDataParser = translationDataParser;
        this.translationPublisher = translationPublisher;
        this.useCache = useCache;
        this.waitBetweenRequestsMs = waitBetweenRequestsMs;
    }

    public void load(List<WordInfo> wordInfos) throws IOException {
        LOG.info(format("source [%s] - loading has started...", translationDataParser.getSource()));
        requestStopwatch = Stopwatch.createUnstarted();
        var addedWordInfos = new HashSet<WordInfo>();
        for (var wordInfo : wordInfos) {
            if (addedWordInfos.contains(wordInfo)) {
                throw new RuntimeException(format("source [%s] - wordInfo [%s] has been already added - there are some duplicated words it seems", translationDataParser.getSource(), wordInfo));
            }

            translationDataParser.parseAndPublish(wordInfo, getDataForForeignWord(wordInfo.getParsedForeignWord()), translationPublisher);

            addedWordInfos.add(wordInfo);
        }
        LOG.info(format("source [%s] - loading complete", translationDataParser.getSource()));
    }

    private InputStream getDataForForeignWord(String foreignWord) throws IOException {
        if (useCache) {
            File cacheFile = getCacheFileForWord(foreignWord);
            if (cacheFile.exists()) {
                LOG.info(format("source [%s] - using the cached file for word=%s", translationDataParser.getSource(), foreignWord));
                return new FileInputStream(cacheFile);
            }
            LOG.info(format("source [%s] - no cached file for word=%s - downloading content...", translationDataParser.getSource(), foreignWord));
        }

        while (requestStopwatch.isRunning() && requestStopwatch.elapsed().toMillis() < waitBetweenRequestsMs) {
            sleep(waitBetweenRequestsMs / 50);
        }

        InputStream translationData = translationDataDownloader.download(foreignWord);

        requestStopwatch.reset().start();

        if (useCache) {
            File cacheFile = saveTranslationDataToCacheFile(foreignWord, translationData);
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
        return new File(CACHES_DIR + translationDataParser.getSource() + "-cache-" + translationDataDownloader.getDictionary() + "/" + word);
    }

}
