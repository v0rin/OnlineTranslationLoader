package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.vorin.bestwords.AppConfig.*;


/** @Tool-do-redagowania-wordlisty

nie zrobic z tego zbyt skomplikowanego zadania - zrob pierwsza czesc do tego pierwszego jezyka, z kolejnymi jezykami bedziesz dodawal kolejne elementy
na razie moze po prostu wyswietlanie tych slowek na stronce zebym mogl udostepnic + checkbox "checked" i comment dla tlumacza
- najlepsze podejscie to Google Sheets
https://support.google.com/docs/answer/3093342?hl=en
https://www.youtube.com/watch?v=nyr3EJH0lTY
*/

/** @Weryfikacje-tlumaczen
 *
zmergowac and check, cross-check translations and example sentences - ustawic alerty gdzie sa problemy
nastepnie porownac, zmergowac i tam gdzie sa problemy/sugestie to ustawic alert i ew. pokazac wszystkie opcje
zrobic test moich 34 slowek z polskim

meanings:
- has to be in SpanishCombined2954.txt (it is Spanish1kNeri i Spanish3kAndki2134488481)
- search for () and other weird characters, search for spaces (two or more words)
- cannot be more than 3 meanings - it is the first 1000 words - don't make it too complicated
- often should be just one - I would assume at least 2/3
- check if google reverse gives the original meaning as the main one
- cross check if it appears in 2 other dictionaries - possibly also reverse check, we'll see how it works
- liczby - powinny miec tylko jedno znaczenie - poza one?
- nie ma znaczen

example sentences:
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
 * split caches by dict
 * move the app to bestwords
 */

public class TranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    public static void main(String... argvs) throws IOException {

        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList34.xml");

        createGoogleWordList(Dictionary.EN_ES, wordInfos,"googleTranslateWordList.xml");

        // reverse wordlist
        createGoogleWordList(Dictionary.ES_EN,
                Util.getReverseForeignWordsFromXml(RES_DIR + "googleTranslateWordList.xml"),
                "googleTranslateReverseWordList.xml");

//        createWordReferenceWordList(Dictionary.EN_ES, wordInfos, "wordReferenceWordList.xml");
//
//        createLingueeWordList(Dictionary.EN_ES, wordInfos, "lingueeWordList.xml");
//
//        createCollinsWordList(Dictionary.ES_EN,
//                              Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + "googleTranslateWordList.xml"),
//                              "collinsReverseWordList.xml");

    }


    private static void createGoogleWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new GoogleTranslateDownloader(dict);
        var parser = new GoogleTranslateParser(0.01, 5);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createWordReferenceWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new WordReferenceDownloader(dict);
        var parser = new WordReferenceParser();
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createLingueeWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new LingueeDownloader(dict);
        var parser = new LingueeParser();
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }


    private static void createCollinsWordList(Dictionary dict, List<WordInfo> wordInfos, String outputXml) throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + outputXml));
        var downloader = new CollinsDownloader(dict);
        var parser = new CollinsSentencesParser(36);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }

}
