package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.Util;

import java.io.*;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GoogleTranslateSynonymTest {

    private static final WordInfo TEST_WORD_INFO = new WordInfo("llamada", null);
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/GoogleTranslate/" + TEST_WORD_INFO.getForeignWord();

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var parser = new GoogleTranslateSynonymParser();
        var publisher = new SynonymStore(parser);
        Set<String> expectedSynonyms = Util.loadWordsFromTxtFile(new File(AppConfig.TEST_RES_DIR + "loaders/GoogleTranslate/llamada-expected-synonyms.txt"));

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_WORD_INFO, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getSynonyms(TEST_WORD_INFO.getForeignWord()), is(expectedSynonyms));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new GoogleTranslateDownloader(Dictionary.ES_EN);

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO.getForeignWord())) {

            // creating a test file
            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)) {
                IOUtils.copy(downloadedDataIS, fos);
            }
//            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
