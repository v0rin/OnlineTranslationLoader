package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Dictionary;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.vorin.bestwords.util.Sources.COLLINS_SOURCE;

public class CollinsTest {

    private static final WordInfo TEST_WORD_INFO = new WordInfo("tomar", "take");
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/Collins/" + TEST_WORD_INFO.getForeignWord();

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new CollinsSentencesParser(30);

        var expectedWordlist = new Wordlist();
        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "take", null, COLLINS_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "take", "lo toma o lo deja - take it or leave it", COLLINS_SOURCE);

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_WORD_INFO, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordlist(), is(expectedWordlist));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new CollinsDownloader(Dictionary.ES_EN);

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO.getForeignWord())) {

            // creating a test file
            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)) {
                IOUtils.copy(downloadedDataIS, fos);
            }
//            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
