package org.vorin.bestwords;

import org.junit.Test;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.util.Sources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class WordlistProcessorTest {

    private static final Dictionary DICT = Dictionary.EN_ES;

    @Test
    public void testCombineMeanings() throws IOException {
        // given
        File googleTranslateWordlistFile = new File(AppConfig.TEST_RES_DIR + DICT.name() + "-GoogleTranslateWordlistTest.xml");
        File lingueeWordlistFile = new File( AppConfig.TEST_RES_DIR + DICT.name() + "-LingueeWordlistTest.xml");
        Wordlist expectedWordlist = Wordlist.loadFromXml(new File(AppConfig.TEST_RES_DIR + DICT.name() + "-WordlistProcessorCombineMeaningsTestResult.xml"));

        var wordlists = Map.of(
                Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml( googleTranslateWordlistFile),
                Sources.LINGUEE_SOURCE, Wordlist.loadFromXml( lingueeWordlistFile));

        var translation = new Translation("carpet", "", "", new ArrayList<>());

        // when
        WordlistProcessor.combineMeanings(translation, wordlists);

        var wordlist = new Wordlist();
        wordlist.getTranslations().add(translation);

        // then
        assertThat(wordlist, is(expectedWordlist));
    }

}