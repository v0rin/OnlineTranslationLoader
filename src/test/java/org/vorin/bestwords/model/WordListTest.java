package org.vorin.bestwords.model;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class WordListTest {

    private Meaning trabajarMeaning;
    private Meaning funcionarMeaning;
    private Meaning tomarMeaning;

    @Before
    public void setUp() {
        trabajarMeaning = new Meaning("trabajar", "trabajar sentence", null, "test meaning src", "test sentence src");
        funcionarMeaning = new Meaning("funcionar", "funcionar sentence", null, "test meaning src", "test sentence src");

        tomarMeaning = new Meaning("tomar", "tomar sentence", null, "test meaning src", "test sentence src");
    }

    @Test
    public void addMeaningsAndExampleSentences() {
        // given
        WordList wl = new WordList();

        // when
        wl.addMeaning("work", "trabajar", "test meaning src");
        wl.addMeaning("work", "funcionar", "test meaning src");
        wl.addMeaning("take", "tomar", "test meaning src");

        wl.addExampleSentence("work", "trabajar", "trabajar sentence", "test sentence src");
        wl.addExampleSentence("work", "funcionar", "funcionar sentence", "test sentence src");
        wl.addExampleSentence("take", "tomar", "tomar sentence", "test sentence src");

        // then
        assertThat(wl.getTranslations().size(), is(2));

        assertThat(wl.getTranslations().get(0).foreignWord, is("work"));
        assertThat(wl.getTranslations().get(0).getAudioName(), is("work"));
        assertThat(wl.getTranslations().get(0).getPronunciation(), containsString("/")); // there were some not recognised weird characters so that is good enough
        assertThat(wl.getTranslations().get(0).getMeanings().size(), is(2));
        assertThat(wl.getTranslations().get(0).getMeanings().get(0), is(trabajarMeaning));
        assertThat(wl.getTranslations().get(0).getMeanings().get(1), is(funcionarMeaning));

        assertThat(wl.getTranslations().get(1).foreignWord, is("take"));
        assertThat(wl.getTranslations().get(1).getAudioName(), is("take"));
        assertThat(wl.getTranslations().get(1).getPronunciation(), containsString("/"));
        assertThat(wl.getTranslations().get(1).getMeanings().size(), is(1));
        assertThat(wl.getTranslations().get(1).getMeanings().get(0), is(tomarMeaning));
    }

    @Test
    public void findTranslationForWord() {
        // given
        WordList wl = new WordList();
        var expectedTakeTranslation = new Translation("take", "/teik/", "take", Arrays.asList(tomarMeaning));

        // when
        wl.addMeaning("work", "trabajar", "test meaning src");
        wl.addMeaning("work", "funcionar", "test meaning src");
        wl.addMeaning("take", "tomar", "test meaning src");

        // then
        assertThat(wl.findTranslationForWord("take").getForeignWord(), is("take"));
    }

    @Test
    public void findMeaning() {
        // given
        WordList wl = new WordList();

        // when
        wl.addMeaning("work", "trabajar", "test meaning src");
        wl.addMeaning("work", "funcionar", "test meaning src");
        wl.addMeaning("take", "tomar", "test meaning src");

        var workTranslation = wl.findTranslationForWord("work");

        // then
        assertThat(wl.findMeaning(workTranslation, "funcionar").getWordMeaning(), is("funcionar"));
    }

    @Test
    public void xmlLoadAndWrite() throws IOException {
        // given
        File originalWordListXmlFile = new File(AppConfig.TEST_RES_DIR + "model/WordList/testEnglishWordList25.xml");
        File tmpWordListXmlFile = new File(AppConfig.TEST_RES_DIR + "model/WordList/tmpTestEnglishWordList25.xml");

        // when
        var wordlist = WordList.loadFromXml(originalWordListXmlFile);
        wordlist.writeToXml(tmpWordListXmlFile);

        // then
        assertThat(wordlist.size(), is(25));
        var t = wordlist.getTranslations().get(8);
        assertThat(t.getForeignWord(), is("can"));
        assertThat(t.getMeanings().get(1).getWordMeaning(), is("puszka"));
        assertTrue(FileUtils.contentEquals(originalWordListXmlFile, tmpWordListXmlFile));

        tmpWordListXmlFile.delete();
    }
}
