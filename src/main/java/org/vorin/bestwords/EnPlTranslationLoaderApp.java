package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.LangUtil;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.vorin.bestwords.AppConfig.*;


public class EnPlTranslationLoaderApp {

    private static final Logger LOG = Logger.get(EnEsTranslationLoaderApp.class);

    public static void main(String... argvs) throws IOException {
//        createWordLists();
        processWordList();
    }

    private static void processWordList() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EN_PL-GoogleTranslateWordList.xml"));

        var wordListProcessor = new WordListProcessor(Dictionary.EN_PL);
        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordListProcessor.processTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordListProcessor.verifyWordList(w);

        w.writeToXml(new File(RES_DIR + "EN_PL-ProcessedWordList.xml"));
    }

    private static void createWordLists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList34.xml");

//        createGoogleWordList(Dictionary.EN_PL, wordInfos,"EN_PL-GoogleTranslateWordList.xml");

        // reverse wordlist
        createGoogleWordList(Dictionary.PL_EN,
                Util.getReverseForeignWordsFromXml(RES_DIR + "EN_PL-GoogleTranslateWordList.xml"),
                "EN_PL-GoogleTranslateReverseWordList.xml");

        createWordReferenceWordList(Dictionary.EN_PL, wordInfos, "EN_PL-WordReferenceWordList.xml");
//
        createLingueeWordList(Dictionary.EN_PL, wordInfos, "EN_PL-LingueeWordList.xml");
    }

    private static void createGoogleWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new GoogleTranslateDownloader(dict);
        var parser = new GoogleTranslateParser(0.01, 5);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 5000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createWordReferenceWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new WordReferenceDownloader(dict);
        var parser = new WordReferenceParser(dict, LangUtil::santizeSpanishMeaning);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createLingueeWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new LingueeDownloader(dict);
        var parser = new LingueeParser();
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }

}
