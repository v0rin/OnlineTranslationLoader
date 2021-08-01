package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.LangUtil;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Sources;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.vorin.bestwords.AppConfig.*;

/**
 * I wanted to be able to automatically download good meanings for a list of words in different languages
 * to be able to use BestWords for diff languages
 * The goal was to create a reasonable list that later would be reviewed by a bilingual person (a pro translator?)
 * the list could include tips for translator where to pay more attention
 *
 * I also had an idea to check if reverse check for a meaning gives the foreign word as the main one e.g.
 * foreign word take -> spanish word tomar so I check if tomar gives take in reverse check
 *
 * The general idea is to have a list of words and then go to multiple dictionary sites
 * and download meanings with word types and example sentences
 * and then being able to select the most important meanings and filter out shitty ones
 * trying to use synonyms e.g. look here {@link WordListProcessor#processMeaningsForTranslation}
 *
 * there is cache mechanism when using a {@link TranslationLoader} class
 *
 * Important Classes and methods:
 * {@link WordListProcessor} - a bit of a bucket for diff stuff
 * {@link WordListProcessor#processMeaningsForTranslation}
 * {@link TranslationLoader}
 *
 * Also read TODO.yaml in the main dir
 *
 */
public class TranslationLoaderApp {

    // TODO rename WordList to wordlist everywhere

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

    private static final SynonymStore SYNONYM_STORE = new SynonymStore(REVERSE_DICT, SYNONYM_PARSER);
    private static final WordListProcessor WORD_LIST_PROCESSOR = new WordListProcessor(DICT, SYNONYM_STORE);


    public static void main(String... argvs) throws IOException {
//        createWordLists();
//        createCombinedWordList();
//        loadSynonyms();
        processWordList();
    }


    private static void createCombinedWordList() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EnglishWordList35.xml"));

        var wordlists = Map.of(
                Sources.GOOGLE_TRANSLATE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordList.xml")),
                Sources.LINGUEE_SOURCE, WordList.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordList.xml")),
                Sources.WORD_REFERENCE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordList.xml")));

        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            WORD_LIST_PROCESSOR.combineMeanings(t, wordlists);
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


    static void createGoogleWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new GoogleTranslateDownloader(dict);
        var parser = new GoogleTranslateParser(0.01, 5);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createWordReferenceWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new WordReferenceDownloader(dict);
        var parser = new WordReferenceParser(dict, getMeaningSanitizer(dict));
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createLingueeWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new LingueeDownloader(dict);
        var parser = new LingueeParser(getMeaningSanitizer(dict));
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createCollinsWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new CollinsDownloader(dict);
        var parser = new CollinsSentencesParser(36);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static Function<String, String> getMeaningSanitizer(Dictionary dict) {
        switch (dict) {
            case EN_ES:
                return LangUtil::sanitizeSpanishMeaning;
            case EN_PL:
                return LangUtil::sanitizePolishMeaning;
            default:
                throw new UnsupportedOperationException("no sanitizer for dict:" + dict.name());
        }
    }
}
