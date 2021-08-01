package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.util.LangUtil;
import org.vorin.bestwords.util.Util;

import java.io.*;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SynonimNetTest {

    private static final String DOMAGAC_SIE = LangUtil.hexStringToUtf8String("64 6f 6d 61 67 61 c4 87 20 73 69 c4 99"); // domagać się - https://onlineutf8tools.com/convert-utf8-to-bytes
    private static final WordInfo TEST_WORD_INFO = new WordInfo(DOMAGAC_SIE, null);
    private static final String TEST_CACHE_FILE_PATH = AppConfig.TEST_RES_DIR + "loaders/SynonimNet/" + TEST_WORD_INFO.getForeignWord();

    @Test
    public void parseAndPublish() throws IOException {
        // given
        var parser = new SynonimNetParser();
        var publisher = new SynonymStore(Dictionary.EN_PL, parser);
        Set<String> expectedSynonyms = Util.loadWordsFromTxtFile(new File(AppConfig.TEST_RES_DIR + "loaders/SynonimNet/domagac-sie-synonyms.txt"));

        // when
        try (var canCacheFileIS = new FileInputStream(new File(TEST_CACHE_FILE_PATH))) {
            parser.parseAndPublish(TEST_WORD_INFO, canCacheFileIS, publisher);
        }

        // then
        assertThat(publisher.getSynonyms(DOMAGAC_SIE), is(expectedSynonyms));
    }

    @Ignore
    @Test
    public void downloadTest() throws IOException {
        var downloader = new SynonimNetDownloader();

        try (var downloadedDataIS = downloader.download(TEST_WORD_INFO.getForeignWord())) {

            // creating a test file
            try(OutputStream fos = new FileOutputStream(TEST_CACHE_FILE_PATH)) {
                IOUtils.copy(downloadedDataIS, fos);
            }
//            System.out.println(IOUtils.toString(downloadedDataIS, StandardCharsets.UTF_8));
        }
    }
}
