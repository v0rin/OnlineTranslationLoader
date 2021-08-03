package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.model.Wordlist;
import org.vorin.bestwords.model.Dictionary;
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
 * trying to use synonyms e.g. look here {@link WordlistProcessor#processMeaningsForTranslation}
 *
 * there is cache mechanism when using a {@link TranslationLoader} class
 *
 * Important Classes and methods:
 * {@link WordlistProcessor} - a bit of a bucket for diff stuff
 * {@link WordlistProcessor#processMeaningsForTranslation}
 * {@link TranslationLoader}
 *
 * Also read TODO.yaml in the main dir
 *
 */
public class TranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    private static final int MAX_MEANING_COUNT_FROM_SRC = 5;

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

    public static void main(String... argvs) throws IOException {
        createWordlists();
//        createCombinedWordlist();
//        loadSynonyms();
//        processWordlist();
    }


    private static void createCombinedWordlist() throws IOException {
        WordlistProcessor wordlistProcessor = new WordlistProcessor(DICT, SYNONYM_STORE);
        var w = Wordlist.loadFromXml(new File(RES_DIR + "EnglishWordlist35.xml"));

        var wordlists = Map.of(
                Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml")),
                Sources.LINGUEE_SOURCE, Wordlist.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordlist.xml")),
                Sources.WORD_REFERENCE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordlist.xml")));

        for (var t : w.getTranslations()) {
            t.setMeanings(new ArrayList<>());
            wordlistProcessor.combineMeanings(t, wordlists);
        }

        w.writeToXml(new File(RES_DIR + DICT.name() + "-CombinedWordlist.xml"));
    }


    private static void processWordlist() throws IOException {
        WordlistProcessor wordlistProcessor = new WordlistProcessor(DICT, SYNONYM_STORE);
        // var w = Wordlist.loadFromXml(new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml"));
//        var w = Wordlist.loadFromXml(new File(RES_DIR + "EnglishWordlist35.xml"));
        var w = Wordlist.loadFromXml(new File(RES_DIR + DICT.name() + "-CombinedWordlist.xml"));

        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordlistProcessor.processMeaningsForTranslation(t)) {
                wordsWithProblemsCount++;
            }
            wordlistProcessor.processExampleSentencesForTranslation(t);
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordlistProcessor.verifyWordlist(w);

        w.writeToXml(new File(RES_DIR + DICT.name() + "-ProcessedWordlist.xml"));
    }


    private static void loadSynonyms() throws IOException {
        var wordInfos = Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + DICT.name() + "-CombinedWordlist.xml");
        var loader = new TranslationLoader(SYNONYM_DOWNLOADER, SYNONYM_PARSER, SYNONYM_STORE, true, 500);
        loader.load(wordInfos);
    }


    private static void createWordlists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordlist35.xml");

        createGoogleWordlist(DICT, wordInfos, RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml", MAX_MEANING_COUNT_FROM_SRC);

        // reverse wordlist
        createGoogleWordlist(REVERSE_DICT,
                Util.getReverseForeignWordsFromXml(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml"),
                RES_DIR + DICT.name() + "-GoogleTranslateReverseWordlist.xml", MAX_MEANING_COUNT_FROM_SRC);

        createWordReferenceWordlist(DICT, wordInfos,  RES_DIR + DICT.name() + "-WordReferenceWordlist.xml");
//
        createLingueeWordlist(DICT, wordInfos,  RES_DIR + DICT.name() + "-LingueeWordlist.xml", MAX_MEANING_COUNT_FROM_SRC);

        createCollinsWordlist(DICT, wordInfos,  RES_DIR + DICT.name() + "-CollinsWordlist.xml");
    }


    static void createGoogleWordlist(Dictionary dict, List<WordInfo> wordInfos, String outputXml, int maxMeaningCount) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(outputXml));
        var downloader = new GoogleTranslateDownloader(dict);
        var parser = new GoogleTranslateParser(0.01, maxMeaningCount);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createWordReferenceWordlist(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(outputXml));
        var downloader = new WordReferenceDownloader(dict);
        var parser = new WordReferenceParser(dict, getMeaningSanitizer(dict));
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createLingueeWordlist(Dictionary dict, List<WordInfo> wordInfos, String outputXml, int maxMeaningCount) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(outputXml));
        var downloader = new LingueeDownloader(dict);
        var parser = new LingueeParser(getMeaningSanitizer(dict), maxMeaningCount);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    static void createCollinsWordlist(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(outputXml));
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
