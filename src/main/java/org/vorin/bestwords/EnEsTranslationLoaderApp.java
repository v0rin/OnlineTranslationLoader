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

import static org.vorin.bestwords.AppConfig.RES_DIR;



/** @Tool-do-redagowania-wordlisty

nie zrobic z tego zbyt skomplikowanego zadania - zrob pierwsza czesc do tego pierwszego jezyka, z kolejnymi jezykami bedziesz dodawal kolejne elementy
na razie moze po prostu wyswietlanie tych slowek na stronce zebym mogl udostepnic + checkbox "checked" i comment dla tlumacza
- najlepsze podejscie to Google Sheets
https://support.google.com/docs/answer/3093342?hl=en
https://www.youtube.com/watch?v=nyr3EJH0lTY
 */

/** @Weryfikacje-tlumaczen

https://www.wordreference.com/enpl/make - rozmieszac
sciagac kilka innych 3k spanish wordlists
sciagnac first 3k dla polskiego
synonimy - polski, spanish

zmergowac and check, cross-check translations and example sentences - ustawic alerty gdzie sa problemy
zrobic test moich 34 slowek z polskim

 @meanings:
 - remove synonyms
 - has to be in SpanishCombined2954.txt (it is Spanish1kNeri i Spanish3kAndki2134488481)
 - search for () and other weird characters, search for spaces (two or more words)
 - cannot be more than 3 meanings - it is the first 1000 words - don't make it too complicated
 - often should be just one - I would assume at least 2/3
 - check if google reverse gives the original meaning as the main one
 - cross check if it appears in 2 other dictionaries - possibly also reverse check, we'll see how it works
 - liczby - powinny miec tylko jedno znaczenie - poza one?
 - nie ma znaczen

 @example sentences:
 - oznacz jako podejrzane jezeli za dlugie sprawdz, jakie sa najdluzsze w polskim - ew. jakie sie miesza najlepiej na ekranie
 - nie moze tez byc za krotkie
 - sprawdz czy zawiera angielskie slow, z ew. formami przeszlymi albo w liczbie mnogiej (nie powinno byc ich az tak duzo, moge manualnie je wpisac do kazdego slowa) - jak nie to alert
 - znalezc 2-3 zrodla (nie tylko wordreference) i jak jest alert to miec wybor
 - the example sentences need to be checked by me, if they really contain the wordMeaning
 choc moge zrobic automatyczny mechanizm, ze jezeli zawiera pierwsze 3 litery z czasownika, albo w sumie regule na regularne to automatycznie jest okroic
 a sprawdze tylko te co nie przejda tej reguly pewnie z max 200
 ale to na samym koncu - jak juz bede mial decolowa liste to wtedy bedzie jakas seria checkow, z tymi alertami
 - search for () and other weird characters
 - nie ma przykladowego zdania
 */

/**
 * TODO @af
 * move the app to bestwords
 */

public class EnEsTranslationLoaderApp {

    private static final Logger LOG = Logger.get(EnPlTranslationLoaderApp.class);

    public static void main(String... argvs) throws IOException {
        createWordLists();
        processWordList();
    }

    private static void processWordList() throws IOException {
        var w = WordList.loadFromXml(new File(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"));

        var wordListProcessor = new WordListProcessor(Dictionary.EN_ES);
        int wordsWithProblemsCount = 0;
        for (var t : w.getTranslations()) {
            if(!wordListProcessor.processTranslation(t)) {
                wordsWithProblemsCount++;
            }
        }

        LOG.info("wordsWithProblemsCount=" + wordsWithProblemsCount);
        wordListProcessor.verifyWordList(w);

        w.writeToXml(new File(RES_DIR + "EN_ES-ProcessedWordList.xml"));
    }

    private static void createWordLists() throws IOException {
        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList34.xml");

        createGoogleWordList(Dictionary.EN_ES, wordInfos,"EN_ES-GoogleTranslateWordList.xml");

        // reverse wordlist
        createGoogleWordList(Dictionary.ES_EN,
                Util.getReverseForeignWordsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"),
                "EN_ES-GoogleTranslateReverseWordList.xml");

        createWordReferenceWordList(Dictionary.EN_ES, wordInfos, "EN_ES-WordReferenceWordList.xml");

        createLingueeWordList(Dictionary.EN_ES, wordInfos, "EN_ES-LingueeWordList.xml");

        createCollinsWordList(Dictionary.ES_EN,
                Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + "EN_ES-GoogleTranslateWordList.xml"),
                "EN_ES-CollinsReverseWordList.xml");
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


    private static void createCollinsWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new CollinsDownloader(dict);
        var parser = new CollinsSentencesParser(36);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }

}
