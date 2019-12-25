package org.vorin.bestwords;

import org.vorin.bestwords.loaders.GoogleTranslateDownloader;
import org.vorin.bestwords.loaders.GoogleTranslateParser;
import org.vorin.bestwords.loaders.TranslationLoader;
import org.vorin.bestwords.loaders.XmlTranslationPublisher;
import org.vorin.bestwords.model.Meaning;
import org.vorin.bestwords.model.Translation;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.Logger;

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
zrobic parsowanie dla tych 3 slownikow ponizej najpierw - zaczac od example sentences bo tego jeszcze nie mam, zeby w praktyce przetestowac

stworzyc parser do kolejnego na zasadzie kopiuj i wklej na poczatek, a potem ujednolicic
zrobic test na podstawie jakiegos pliku z cache dla kazdego loadera


powinno zaladowac translations i przykladowe zdania z roznych zrodel
niektore zrodla beda mialy tylko przykladowe zdania, niektore tlumaczenie, a niektore oba
chyba wszystkie powinny tworzyc xml a potem mergowalbym xml
te zrodla ktore maja tylko zdania beda pracowac na meanings
moze powinienem miec 2 osobne tory, jeden dla meanings drugi dla example sentences
nastepnie porownac, zmergowac i tam gdzie sa problemy/sugestie to ustawic alert i ew. pokazac wszystkie opcje

https://www.wordreference.com/es/en/translation.asp?spen=tomar
class="FrEx"
class="ToEx"
https://www.collinsdictionary.com/dictionary/spanish-english/tomar - taki sam jak zakladka collins w wordreference ale moze latwiejszy do parsowania
https://www.linguee.com/english-spanish/search?source=auto&query=work
https://www.linguee.com/english-spanish/search?source=auto&query=tomar

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

        createGoogleWordList();
//        createReverseWordList();
    }


    private static void createGoogleWordList() throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + "googleTranslateMeaningsLoaderWordlist.xml"));
        var downloader = new GoogleTranslateDownloader(Dictionary.EN_ES);
        var parser = new GoogleTranslateParser(0.01, 5);
        var googleTranslateMeaningLoader = new TranslationLoader(downloader, parser, xmlPublisher, GOOGLE_TRANSLATE_SOURCE, true);
        var words = getForeignWordsFromXml(RES_DIR + "EnglishWordList25.xml");

        googleTranslateMeaningLoader.load(words);
        xmlPublisher.writeToTarget();
    }


    private static void createReverseGoogleWordList() throws IOException {
        var xmlPublisher = new XmlTranslationPublisher(new File(RES_DIR + "googleTranslateMeaningsLoaderReverseWordlist.xml"));
        var downloader = new GoogleTranslateDownloader(Dictionary.ES_EN);
        var parser = new GoogleTranslateParser(0.01, 5);
        var googleTranslateMeaningLoader = new TranslationLoader(downloader, parser, xmlPublisher, GOOGLE_TRANSLATE_SOURCE, true);
        var words = getForeignWordsFromXml(RES_DIR + "googleTranslateMeaningsLoaderWordlist.xml");

        googleTranslateMeaningLoader.load(words);
        xmlPublisher.writeToTarget();
    }


    private static List<String> getForeignWordsFromXml(String xmlPath) throws IOException {
        var wl = WordList.loadFromXml(new File(xmlPath));
        return wl.getTranslations().stream().map(Translation::getForeignWord).distinct().collect(toList());
    }

}
