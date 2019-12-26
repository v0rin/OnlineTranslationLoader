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
import static org.junit.Assert.*;
import static org.vorin.bestwords.util.Sources.GOOGLE_TRANSLATE_SOURCE;

public class GoogleTranslateTest {

    private static final WordInfo TEST_WORD_INFO = new WordInfo("can", null);
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/GoogleTranslate/" + TEST_WORD_INFO.getForeignWord();


    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new GoogleTranslateParser(0.01, 5);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "lata", GOOGLE_TRANSLATE_SOURCE);
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "bote", GOOGLE_TRANSLATE_SOURCE);
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "poder", GOOGLE_TRANSLATE_SOURCE);

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_WORD_INFO, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new GoogleTranslateDownloader(Dictionary.EN_ES);

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO.getForeignWord())) {
//            // creating a test file
//            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)){
//                IOUtils.copy(downloadedDataIS, fos);
//            }
            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}