package org.vorin.bestwords;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.exceptions.UnirestException;

import static org.vorin.bestwords.Util.print;


/*
compare main translations from diff sources and alert when they are not matching

https://www.wordreference.com/es/en/translation.asp?spen=tomar
class="FrEx"
class="ToEx"
https://www.collinsdictionary.com/dictionary/spanish-english/tomar - taki sam jak zakladka collins w wordreference ale moze latwiejszy do parsowania
https://www.spanishdict.com/translate/ella
https://www.duolingo.com/dictionary/Spanish/trabajar/d343cb4b97a7fa488507f301afcc708e
https://www.linguee.com/english-spanish/search?source=auto&query=work
https://www.linguee.com/english-spanish/search?source=auto&query=tomar

https://www.lexico.com/en-es/translate/work
https://www.lexico.com/es-en/traducir/trabajo

oznacz jako podejrzane jezeli za dlugie sprawdz, jakie sa najdluzsze w polskim - ew. jakie sie miesza najlepiej na ekranie
sprawdz czy zawiera angielskie slow, z ew. formami przeszlymi albo w liczbie mnogiej (nie powinno byc ich az tak duzo, moge manualnie je wpisac do kazdego slowa) - jak nie to alert
znalezc 2-3 zrodla (nie tylko wordreference) i jak jest alert to miec wybor

w sumie moge napisac jakies tool do szybkiego pozniejszego redagowania
przyda sie przy kolejnych jezykach
najlepiej web based, moglbym udostepnic komus to poprawiania
pracuje na pliku
potem latwy eksport do mojego formatu xml - moze tak naprawde oparty na tym formacie xml, z dodatkowymi atrybutami
powinny byc narzedzia do czytania XML - z jakimis opcjami wyswietlania

nie zrobic z tego zbyt skomplikowanego zadania - zrob pierwsza czesc do tego pierwszego jezyka, z kolejnymi jezykami bedziesz dodawal kolejne elementy
na razie moze po prostu wyswietlanie tych slowek na stronce zebym mogl udostepnic + checkbox "checked" i comment dla tlumacza
moze daloby sie to zrobic za pomoc javascript?

https://www.dummies.com/web-design-development/html/how-to-load-xml-with-javascript-on-an-html5-page/
https://stackoverflow.com/questions/19220873/how-to-read-xml-file-contents-in-jquery-and-display-in-html-elements/19220964
http://jsfiddle.net/mVtCD/

wordlist-01-dfas open a page for the file, connects to the db
bedzie prosta db w postgres wordId, checked, comment -> JS bedzie robil posta, ktorego odbierze php, load pobierze xml i dane z bazy i wyswietli

sqlite for php
https://gist.github.com/bladeSk/6294d3266370868601a7d2e50285dbf5

http://phpminiadmin.sourceforge.net/

https://support.google.com/docs/answer/3093342?hl=en
https://www.youtube.com/watch?v=nyr3EJH0lTY
*/

public class TranslationLoader {

    public static void main(String... argvs) throws JsonProcessingException, IOException, UnirestException, InterruptedException {
        // String[] words = {"work", "take"};
        String[] words = {};

        var googleTranslateLoader = new GoogleTranslateLoader();
        googleTranslateLoader.load();
    }

}
