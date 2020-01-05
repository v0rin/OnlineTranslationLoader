package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.LangUtil;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.vorin.bestwords.AppConfig.*;


public class TranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    // ### PL CONFIG ###############
//    private static final Dictionary DICT = Dictionary.EN_PL;
//    private static final Dictionary REVERSE_DICT = Dictionary.PL_EN;
//    private static final TranslationDataParser SYNONYM_PARSER = new SynonimNetParser();
//    private static final TranslationDataDownloader SYNONYM_DOWNLOADER = new SynonimNetDownloader();
    // ##########################

    // ### ES CONFIG ###############
    private static final Dictionary DICT = Dictionary.EN_ES;
    private static final Dictionary REVERSE_DICT = Dictionary.ES_EN;
    private static final TranslationDataParser SYNONYM_PARSER = new GoogleTranslateSynonymParser();
    private static final TranslationDataDownloader SYNONYM_DOWNLOADER = new GoogleTranslateDownloader(REVERSE_DICT);
    // ##########################

    private static final SynonymStore SYNONYM_STORE = new SynonymStore(SYNONYM_PARSER);
    private static final WordListProcessor WORD_LIST_PROCESSOR = new WordListProcessor(DICT, SYNONYM_PARSER);

    public static void main(String... argvs) throws IOException {
        createWordLists();
        createCombinedWordList();
        loadSynonyms();
//        processWordList();
    }

    private static void createCombinedWordList() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EnglishWordList35.xml"));

        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            WORD_LIST_PROCESSOR.combineMeanings(t);
        }

        w.writeToXml(new File(RES_DIR + DICT.name() + "-CombinedWordList.xml"));
    }

    private static void processWordList() throws IOException {
        // var w = WordList.loadFromXml(new File(RES_DIR + DICT.name() + "-GoogleTranslateWordList.xml"));
//        var w = WordList.loadFromXml(new File(RES_DIR + "EnglishWordList35.xml"));
        var w = WordList.loadFromXml(new File(RES_DIR + DICT.name() + "-CombinedWordList.xml"));

        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!WORD_LIST_PROCESSOR.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
            WORD_LIST_PROCESSOR.processExampleSentencesForTranslation(t);
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        WORD_LIST_PROCESSOR.verifyWordList(w);

        w.writeToXml(new File(RES_DIR + DICT.name() + "-ProcessedWordList.xml"));
    }

    private static void loadSynonyms() throws IOException {
        var wordInfos = Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + DICT.name() + "-CombinedWordList.xml");
        var loader = new TranslationLoader(SYNONYM_DOWNLOADER, SYNONYM_PARSER, SYNONYM_STORE, true, 500);
        loader.load(wordInfos);
    }

    private static void createWordLists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList35.xml");

        createGoogleWordList(DICT, wordInfos, DICT.name() + "-GoogleTranslateWordList.xml");

        // reverse wordlist
        createGoogleWordList(REVERSE_DICT,
                Util.getReverseForeignWordsFromXml(RES_DIR + DICT.name() + "-GoogleTranslateWordList.xml"),
                DICT.name() + "-GoogleTranslateReverseWordList.xml");

        createWordReferenceWordList(DICT, wordInfos,  DICT.name() + "-WordReferenceWordList.xml");
//
        createLingueeWordList(DICT, wordInfos,  DICT.name() + "-LingueeWordList.xml");
    }

    private static void createGoogleWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new GoogleTranslateDownloader(dict);
        var parser = new GoogleTranslateParser(0.01, 5);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createWordReferenceWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new WordReferenceDownloader(dict);
        var parser = new WordReferenceParser(dict, LangUtil::sanitizePolishMeaning);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createLingueeWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new LingueeDownloader(dict);
        var parser = new LingueeParser(LangUtil::sanitizePolishMeaning);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }

}
