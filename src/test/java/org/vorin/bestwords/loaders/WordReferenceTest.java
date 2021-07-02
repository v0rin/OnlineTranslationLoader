package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.LangUtil;

import java.io.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.vorin.bestwords.util.Dictionary.EN_ES;
import static org.vorin.bestwords.util.Dictionary.EN_PL;
import static org.vorin.bestwords.util.Sources.WORD_REFERENCE_SOURCE;

public class WordReferenceTest {

    private static final WordInfo TEST_WORD_INFO = new WordInfo("can", null);
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/WordReference/" + TEST_WORD_INFO.getForeignWord();
    private static final WordInfo TEST_WORD_INFO_2 = new WordInfo("make", null);
    private static final String TEST_CACHE_FILE_PATH_2 = AppConfig.TEST_RES_DIR + "loaders/WordReference/" + TEST_WORD_INFO_2.getForeignWord();

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new WordReferenceParser(EN_ES, LangUtil::sanitizeSpanishMeaning);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "poder", "", WORD_REFERENCE_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "poder", "I can carry those suitcases for you - puedo llevarte esas maletas", WORD_REFERENCE_SOURCE);

        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "saber", "", WORD_REFERENCE_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "saber", "she can play the piano - ella sabe tocar el piano", WORD_REFERENCE_SOURCE);

        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "ser posible", "", WORD_REFERENCE_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "ser posible", "such things can happen if you're not careful - es posible que pasen cosas asi si no llevas cuidado", WORD_REFERENCE_SOURCE);

        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "bote", "", WORD_REFERENCE_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "bote", "we need three more cans of paint - necesitamos tres botes mas de pintura", WORD_REFERENCE_SOURCE);

        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), "lata", "", WORD_REFERENCE_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), "lata", "pass me that can of peas - pasame esa lata de guisantes", WORD_REFERENCE_SOURCE);

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_WORD_INFO, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }

    @Test
    public void parseAndPublish2() throws IOException {
        // given
        var publisher = new XmlTranslationPublisher(null);
        var parser = new WordReferenceParser(EN_PL, LangUtil::sanitizeSpanishMeaning);

        var expectedWordList = WordList.loadFromXml(new File(AppConfig.TEST_RES_DIR + "loaders/WordReference/EN_PL-make-wordlist.xml"));

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH_2))) {
            parser.parseAndPublish(TEST_WORD_INFO_2, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new WordReferenceDownloader(EN_PL);

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO_2.getForeignWord())) {

            // creating a test file
            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH_2)) {
                IOUtils.copy(downloadedDataIS, fos);
            }
//            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
