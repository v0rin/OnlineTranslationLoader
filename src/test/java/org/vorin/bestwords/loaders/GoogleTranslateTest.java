package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.vorin.bestwords.util.Sources.GOOGLE_TRANSLATE_SOURCE;

public class GoogleTranslateTest {

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new GoogleTranslateParser(0.01, 5);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning("can", "lata", GOOGLE_TRANSLATE_SOURCE);
        expectedWordList.addMeaning("can", "bote", GOOGLE_TRANSLATE_SOURCE);
        expectedWordList.addMeaning("can", "poder", GOOGLE_TRANSLATE_SOURCE);

        // when
        String cacheFilePath = AppConfig.TEST_RES_DIR + "loaders/TranslationLoader/test-source-cache/can";
        try (var canCacheFileIS = new FileInputStream(new File(cacheFilePath))) {
            parser.parseAndPublish("can", canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new GoogleTranslateDownloader(Dictionary.EN_ES);

        try (var downloadedDataIS = downloader.download("take");) {
            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}