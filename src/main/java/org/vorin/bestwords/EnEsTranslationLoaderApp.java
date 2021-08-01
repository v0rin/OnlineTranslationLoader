package org.vorin.bestwords;

import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Sources;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.vorin.bestwords.AppConfig.RES_DIR;


public class EnEsTranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    // ### ES CONFIG ###############
    private static final Dictionary DICT = Dictionary.EN_ES;
    // ##########################

    public static void main(String... args) throws IOException {
//        createWordlists();
        createCombinedWordlist();
//        processWordlist();
    }


    private static void createCombinedWordlist() throws IOException {
        var w = Wordlist.loadFromXml(new File(RES_DIR + "EnglishWordlist35.xml"));

        var wordlists = Map.of(
//                Sources.WORD_REFERENCE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordlist.xml")),
                Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml")),
                Sources.LINGUEE_SOURCE, Wordlist.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordlist.xml")));

        int countLimit = 5;
        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            WordlistProcessor.combineMeanings(t, wordlists);
            if (countLimit-- == 0) break;
        }

        w.writeToXml(new File(RES_DIR + DICT.name() + "-CombinedWordlist.xml"));
    }


    private static void processWordlist() throws IOException {
        var w = Wordlist.loadFromXml(new File(RES_DIR + "EN_ES-GoogleTranslateWordlist.xml"));

        var wordlistProcessor = new WordlistProcessor(Dictionary.EN_ES, null);
        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordlistProcessor.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordlistProcessor.verifyWordlist(w);

        w.writeToXml(new File(RES_DIR + "EN_ES-ProcessedWordlist.xml"));
    }


    private static void createWordlists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordlist35.xml");

        TranslationLoaderApp.createGoogleWordlist(Dictionary.EN_ES, wordInfos,"EN_ES-GoogleTranslateWordlist.xml");

        // reverse wordlist
        TranslationLoaderApp.createGoogleWordlist(Dictionary.ES_EN,
                Util.getReverseForeignWordsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordlist.xml"),
                "EN_ES-GoogleTranslateReverseWordlist.xml");

        TranslationLoaderApp.createWordReferenceWordlist(Dictionary.EN_ES, wordInfos, "EN_ES-WordReferenceWordlist.xml");

        TranslationLoaderApp.createLingueeWordlist(Dictionary.EN_ES, wordInfos, "EN_ES-LingueeWordlist.xml");

        TranslationLoaderApp.createCollinsWordlist(Dictionary.ES_EN,
                Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordlist.xml"),
                "EN_ES-CollinsReverseWordlist.xml");
    }

}
