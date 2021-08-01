package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.LangUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.vorin.bestwords.util.Sources.LINGUEE_SOURCE;

public class LingueeTest {

    private static final WordInfo TEST_WORD_INFO = new WordInfo("can", null);
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/Linguee/" + TEST_WORD_INFO.getForeignWord();

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new LingueeParser(LangUtil::sanitizeSpanishMeaning);

        var expectedWordlist = new Wordlist();
        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "poder", "verb", LINGUEE_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "poder", "cheetahs can run very fast - los guepardos pueden correr muy rapido", LINGUEE_SOURCE);

        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "saber", "verb", LINGUEE_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "saber", "my sister can speak four languages - mi hermana sabe hablar cuatro idiomas", LINGUEE_SOURCE);

        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "enlatar", "verb", LINGUEE_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "enlatar", "the workers can tuna with olive oil - los operarios enlatan el atun con aceite de oliva", LINGUEE_SOURCE);

        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "lata", "noun", LINGUEE_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "lata", "many drinks are sold in plastic bottles or cans - muchas bebidas se venden en botellas de plastico o en latas", LINGUEE_SOURCE);

        expectedWordlist.addMeaning(TEST_WORD_INFO.getForeignWord(), "bote", "noun", LINGUEE_SOURCE);
        expectedWordlist.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "bote", "I have a can of beer and a bottle of water - tengo un bote de cerveza y una botella de agua", LINGUEE_SOURCE);

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
        var downloader = new LingueeDownloader(Dictionary.EN_ES);

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO.getForeignWord())) {

            // creating a test file
//            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)) {
//                IOUtils.copy(downloadedDataIS, fos);
//            }
            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
