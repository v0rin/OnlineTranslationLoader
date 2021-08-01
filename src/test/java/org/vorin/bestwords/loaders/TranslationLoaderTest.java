package org.vorin.bestwords.loaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Dictionary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TranslationLoaderTest {

    private static final String TEST_CONTENT = "TEST_CONTENT";
    private static final String TEST_CONTENT_CACHED = "TEST_CONTENT_CACHED";
    private static final String TEST_SOURCE = "test-source";
    private static final WordInfo TEST_WORD_INFO = new WordInfo("test-foreign-word", "test-meaning");
    private static final String TEST_MEANING1 = "TEST_MEANING1";
    private static final String TEST_MEANING2 = "TEST_MEANING2";
    private static final String TEST_EXAMPLE_SENTENCE = "EXAMPLE_SENTENCE";

    private TranslationPublisher publisher;
    private TestTranslationParser translationDataParser;
    private TestTranslationParser translationCachedDataParser;
    private TestTranslationDownloader translationDownloader;

    @Before
    public void setUp() {
        AppConfig.CACHES_DIR = AppConfig.TEST_RES_DIR + "/loaders/TranslationLoader/";
        translationDataParser = new TestTranslationParser(TEST_CONTENT);
        translationCachedDataParser = new TestTranslationParser(TEST_CONTENT_CACHED);
        translationDownloader = new TestTranslationDownloader();
        publisher = new XmlTranslationPublisher(null);
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyWhenNoCacheDataExists() throws Exception {
        // given
        AppConfig.CACHES_DIR = AppConfig.TEST_RES_DIR + "loaders/TranslationLoader/tmp/";
        String testCacheDir = AppConfig.CACHES_DIR + "test-source-cache-EN_ES/";
        new File(testCacheDir).mkdirs();
        var loader = new TranslationLoader(translationDownloader, translationDataParser, publisher, true);

        var expectedWordlist = getExpectedWordlist();

        // when
        loader.load(Arrays.asList(TEST_WORD_INFO));

        // then
        assertThat(publisher.getWordlist(), is(expectedWordlist));
        String cachedFilePath = testCacheDir + TEST_WORD_INFO.getForeignWord();
        assertTrue(String.format("File [%s] does not exist", cachedFilePath), new File(cachedFilePath).exists());
        try (var cachedFileIS = new FileInputStream(cachedFilePath)) {
            assertThat(IOUtils.toString(cachedFileIS, StandardCharsets.UTF_8), is(TEST_CONTENT));
        }
        FileUtils.deleteDirectory(new File(AppConfig.CACHES_DIR));
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyWithoutUsingCache() throws Exception {
        // given
        var loader = new TranslationLoader(translationDownloader, translationDataParser, publisher, false);

        var expectedWordlist = getExpectedWordlist();

        // when
        loader.load(Arrays.asList(TEST_WORD_INFO));

        // then
        assertThat(publisher.getWordlist(), is(expectedWordlist));
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyFromCache() throws Exception {
        // given
        var loader = new TranslationLoader(translationDownloader, translationCachedDataParser, publisher, true);

        var expectedWordlist = getExpectedWordlist();

        // when
        loader.load(Arrays.asList(TEST_WORD_INFO));

        // then
        assertThat(publisher.getWordlist(), is(expectedWordlist));
    }


    private Wordlist getExpectedWordlist() {
        var expectedWordList = new Wordlist();
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), TEST_MEANING1, null, TEST_SOURCE);
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), TEST_MEANING2, null, TEST_SOURCE);
        expectedWordList.addMeaning(TEST_WORD_INFO.getForeignWord(), TEST_WORD_INFO.getWordMeaning(), null, TEST_SOURCE);
        expectedWordList.addExampleSentence(TEST_WORD_INFO.getForeignWord(), TEST_WORD_INFO.getWordMeaning(), TEST_EXAMPLE_SENTENCE, TEST_SOURCE);
        return expectedWordList;
    }

    static class TestTranslationDownloader implements TranslationDataDownloader {
        @Override
        public InputStream download(String word) throws IOException {
            return new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Dictionary getDictionary() {
            return Dictionary.EN_ES;
        }
    }

    static class TestTranslationParser implements TranslationDataParser {
        private final String expectedContent;
        TestTranslationParser(String expectedContent) {
            this.expectedContent = expectedContent;
        }

        @Override
        public void parseAndPublish(WordInfo wordInfo, InputStream translationData, TranslationPublisher translationPublisher) throws IOException {
            String dataStr = IOUtils.toString(translationData, StandardCharsets.UTF_8);

            if (dataStr.equals(expectedContent)) {
                translationPublisher.addMeaning(wordInfo.getForeignWord(), TEST_MEANING1, TEST_SOURCE);
                translationPublisher.addMeaning(wordInfo.getForeignWord(), TEST_MEANING2, TEST_SOURCE);
                translationPublisher.addMeaning(wordInfo.getForeignWord(), wordInfo.getWordMeaning(), TEST_SOURCE);
                translationPublisher.addExampleSentence(wordInfo.getForeignWord(), wordInfo.getWordMeaning(), TEST_EXAMPLE_SENTENCE, TEST_SOURCE);
            }
            translationData.close();
        }

        @Override
        public String getSource() {
            return TEST_SOURCE;
        }
    }
}