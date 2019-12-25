package org.vorin.bestwords.loaders;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.WordList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TranslationLoaderTest {

    private static final String TEST_CONTENT = "TEST_CONTENT";
    private static final String TEST_CONTENT_CACHED = "TEST_CONTENT_CACHED";
    private static final String TEST_SOURCE = "test-source";
    private static final String TEST_FOREIGN_WORD = "test-foreign-word";
    private static final String TEST_MEANING1 = "TEST_MEANING1";
    private static final String TEST_MEANING2 = "TEST_MEANING2";

    private TranslationPublisher publisher;
    private TestTranslationParser translationDataParser;
    private TestTranslationParser translationCachedDataParser;
    private TestTranslationDownloader translationDownloader;

    @Before
    public void setUp() {
        AppConfig.CACHES_DIR = AppConfig.WORKING_DIR + "/src/test/resources/loaders/TranslationLoader/";
        translationDataParser = new TestTranslationParser(TEST_CONTENT);
        translationCachedDataParser = new TestTranslationParser(TEST_CONTENT_CACHED);
        translationDownloader = new TestTranslationDownloader();
        publisher = new XmlTranslationPublisher(null);
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyWhenNoCacheDataExists() throws Exception {
        // given
        AppConfig.CACHES_DIR = AppConfig.WORKING_DIR + "/src/test/resources/loaders/TranslationLoader/tmp/";
        String testCacheDir = AppConfig.CACHES_DIR + "test-source-cache/";
        new File(testCacheDir).mkdirs();
        var loader = new TranslationLoader(translationDownloader, translationDataParser, publisher, TEST_SOURCE, true);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING1, TEST_SOURCE);
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING2, TEST_SOURCE);

        // when
        loader.load(Arrays.asList(TEST_FOREIGN_WORD));

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
        String cachedFilePath = testCacheDir + TEST_FOREIGN_WORD;
        assertTrue(new File(cachedFilePath).exists());
        try (var cachedFileIS = new FileInputStream(cachedFilePath)) {
            assertThat(IOUtils.toString(cachedFileIS, StandardCharsets.UTF_8), is(TEST_CONTENT));
        }
        FileUtils.deleteDirectory(new File(AppConfig.CACHES_DIR));
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyWithoutUsingCache() throws Exception {
        // given
        var loader = new TranslationLoader(translationDownloader, translationDataParser, publisher, TEST_SOURCE, false);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING1, TEST_SOURCE);
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING2, TEST_SOURCE);

        // when
        loader.load(Arrays.asList(TEST_FOREIGN_WORD));

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }


    @Test
    public void shouldLoadAndParseDataCorrectlyFromCache() throws Exception {
        // given
        var loader = new TranslationLoader(translationDownloader, translationCachedDataParser, publisher, TEST_SOURCE, true);

        var expectedWordList = new WordList();
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING1, TEST_SOURCE);
        expectedWordList.addMeaning(TEST_FOREIGN_WORD, TEST_MEANING2, TEST_SOURCE);

        // when
        loader.load(Arrays.asList(TEST_FOREIGN_WORD));

        // then
        assertThat(publisher.getWordList(), is(expectedWordList));
    }


    static class TestTranslationDownloader implements TranslationDataDownloader {
        @Override
        public InputStream download(String word) throws IOException {
            return new ByteArrayInputStream(TEST_CONTENT.getBytes(StandardCharsets.UTF_8));
        }
    }

    static class TestTranslationParser implements TranslationDataParser {
        private final String expectedContent;
        TestTranslationParser(String expectedContent) {
            this.expectedContent = expectedContent;
        }

        @Override
        public void parseAndPublish(String foreignWord, InputStream translationData, TranslationPublisher translationPublisher) throws IOException {
            String dataStr = IOUtils.toString(translationData, StandardCharsets.UTF_8);

            if (dataStr.equals(expectedContent)) {
                translationPublisher.addMeaning(foreignWord, TEST_MEANING1, TEST_SOURCE);
                translationPublisher.addMeaning(foreignWord, TEST_MEANING2, TEST_SOURCE);
            }
            translationData.close();
        }
    }
}