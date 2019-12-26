package org.vorin.bestwords;

import org.vorin.bestwords.loaders.*;
import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.AppConfig.*;
import static org.vorin.bestwords.util.Sources.*;


/* Tool do redagowania wordlisty:
nie zrobic z tego zbyt skomplikowanego zadania - zrob pierwsza czesc do tego pierwszego jezyka, z kolejnymi jezykami bedziesz dodawal kolejne elementy
na razie moze po prostu wyswietlanie tych slowek na stronce zebym mogl udostepnic + checkbox "checked" i comment dla tlumacza
- najlepsze podejscie to Google Sheets
https://support.google.com/docs/answer/3093342?hl=en
https://www.youtube.com/watch?v=nyr3EJH0lTY

TODO @af
powinno zaladowac translations i przykladowe zdania z roznych zrodel,
nastepnie porownac, zmergowac i tam gdzie sa problemy/sugestie to ustawic alert i ew. pokazac wszystkie opcje

https://www.wordreference.com/es/en/translation.asp?spen=tomar
https://www.collinsdictionary.com/dictionary/spanish-english/tomar - taki sam jak zakladka collins w wordreference ale moze latwiejszy do parsowania
https://www.linguee.com/english-spanish/search?source=auto&query=work
https://www.linguee.com/english-spanish/search?source=auto&query=tomar

move the app to bestwords

CHECKS & ALERTS:
meanings:
- check if the meaning is in SpanishCombined2954.txt

example sentences:
- oznacz jako podejrzane jezeli za dlugie sprawdz, jakie sa najdluzsze w polskim - ew. jakie sie miesza najlepiej na ekranie
- nie moze tez byc za krotkie
- sprawdz czy zawiera angielskie slow, z ew. formami przeszlymi albo w liczbie mnogiej (nie powinno byc ich az tak duzo, moge manualnie je wpisac do kazdego slowa) - jak nie to alert
- znalezc 2-3 zrodla (nie tylko wordreference) i jak jest alert to miec wybor
- the example sentences need to be checked by me, if they really contain the wordMeaning
    choc moge zrobic automatyczny mechanizm, ze jezeli zawiera pierwsze 3 litery z czasownika, albo w sumie regule na regularne to automatycznie jest okroic
         a sprawdze tylko te co nie przejda tej reguly pewnie z max 200
    ale to na samym koncu - jak juz bede mial decolowa liste to wtedy bedzie jakas seria checkow, z tymi alertami

*/

public class TranslationLoaderApp {

    private static final Logger LOG = Logger.get(TranslationLoaderApp.class);

    public static void main(String... argvs) throws IOException {

        var wordInfos = Util.getForeignWordsFromXml(RES_DIR + "EnglishWordList25.xml");

//        createGoogleWordList(Dictionary.EN_ES, wordInfos,"googleTranslateWordList.xml");

//        // reverse wordlist
//        createGoogleWordList(Dictionary.ES_EN,
//                Util.getReverseForeignWordsFromXml(RES_DIR + "googleTranslateWordList.xml"),
//                "googleTranslateReverseWordList.xml");

//        createWordReferenceWordList(Dictionary.EN_ES, wordInfos, "wordReferenceWordList.xml");

//        createLingueeWordList(Dictionary.EN_ES, wordInfos, "lingueeWordList.xml");

        createCollinsWordList(Dictionary.ES_EN,
                              Util.getReverseForeignWordsWithMeaningsFromXml(RES_DIR + "googleTranslateWordList.xml"),
                              "collinsReverseWordList.xml");

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
        var parser = new CollinsSentencesParser(30);
        var loader = new TranslationLoader(downloader, parser, xmlPublisher, true, 1000);

        loader.load(wordInfos);
        xmlPublisher.writeToTarget();
    }

}
