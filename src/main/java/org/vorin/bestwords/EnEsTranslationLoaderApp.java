package org.vorin.bestwords;

import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
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
//        createWordLists();
        createCombinedWordlist();
//        processWordList();
    }


    private static void createCombinedWordlist() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EnglishWordList35.xml"));

        var wordlists = Map.of(
//                Sources.WORD_REFERENCE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordList.xml")),
                Sources.GOOGLE_TRANSLATE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordList.xml")),
                Sources.LINGUEE_SOURCE, WordList.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordList.xml")));

        int countLimit = 5;
        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            WordListProcessor.combineMeanings(t, wordlists);
            if (countLimit-- == 0) break;
        }

        w.writeToXml(new File(RES_DIR + DICT.name() + "-CombinedWordList.xml"));
    }


    private static void processWordList() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"));

        var wordListProcessor = new WordListProcessor(Dictionary.EN_ES, null);
        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordListProcessor.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordListProcessor.verifyWordList(w);

        w.writeToXml(new File(RES_DIR + "EN_ES-ProcessedWordList.xml"));
    }


    private static void createWordLists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList35.xml");

        TranslationLoaderApp.createGoogleWordList(Dictionary.EN_ES, wordInfos,"EN_ES-GoogleTranslateWordList.xml");

        // reverse wordlist
        TranslationLoaderApp.createGoogleWordList(Dictionary.ES_EN,
                Util.getReverseForeignWordsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"),
                "EN_ES-GoogleTranslateReverseWordList.xml");

        TranslationLoaderApp.createWordReferenceWordList(Dictionary.EN_ES, wordInfos, "EN_ES-WordReferenceWordList.xml");

        TranslationLoaderApp.createLingueeWordList(Dictionary.EN_ES, wordInfos, "EN_ES-LingueeWordList.xml");

        TranslationLoaderApp.createCollinsWordList(Dictionary.ES_EN,
                Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"),
                "EN_ES-CollinsReverseWordList.xml");
    }

}
