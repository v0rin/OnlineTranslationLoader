package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.vorin.bestwords.util.Sources.WORD_REFERENCE_SOURCE;

public class WordReferenceTest {

    private static final String TEST_FOREIGN_WORD = "can";
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/WordReference/" + TEST_FOREIGN_WORD;


    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new WordReferenceParser();

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, "lata", WORD_REFERENCE_SOURCE);
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, "bote", WORD_REFERENCE_SOURCE);
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, "poder", WORD_REFERENCE_SOURCE);

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_FOREIGN_WORD, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new WordReferenceDownloader(Dictionary.EN_ES);

        try (var downloadedDataIS = downloader.download(TEST_FOREIGN_WORD);) {

            // creating a test file
            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)) {
                IOUtils.copy(downloadedDataIS, fos);
            }
//            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
