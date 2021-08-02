package org.vorin.bestwords;

import org.vorin.bestwords.loaders.WordInfo;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Sources;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.vorin.bestwords.AppConfig.RES_DIR;


public class EnEsTranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    // ### ES CONFIG ###############
    private static final Dictionary DICT = Dictionary.EN_ES;
    private static final int MAX_MEANING_COUNT_FROM_SRC = 2;
    // ##########################

    public static void main(String... args) throws IOException {
        createWordlists();
        createCombinedWordlist();
//        processWordlist();
    }


    private static void createCombinedWordlist() throws IOException {
        var w = Wordlist.loadFromXml(new File(RES_DIR + DICT.name() + "-input-wordlist.xml"));

        var wordlists = Map.of(
                Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml")),
                Sources.LINGUEE_SOURCE, Wordlist.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordlist.xml")));

        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            WordlistProcessor.combineMeanings(t, wordlists);
        }

        w.writeToXml(new File(RES_DIR + DICT.name() + "-CombinedWordlist.xml"));
    }


    private static void processWordlist() throws IOException {
        var w = Wordlist.loadFromXml(new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml"));

        var wordlistProcessor = new WordlistProcessor(DICT, null);
        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordlistProcessor.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordlistProcessor.verifyWordlist(w);

        w.writeToXml(new File(RES_DIR + DICT.name() + "-ProcessedWordlist.xml"));
    }


    private static void createWordlists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + DICT.name() + "-input-wordlist.xml");

        TranslationLoaderApp.createGoogleWordlist(DICT, wordInfos,DICT.name() + "-GoogleTranslateWordlist.xml", MAX_MEANING_COUNT_FROM_SRC);

        TranslationLoaderApp.createLingueeWordlist(DICT, wordInfos, DICT.name() + "-LingueeWordlist.xml", MAX_MEANING_COUNT_FROM_SRC);
    }

}
