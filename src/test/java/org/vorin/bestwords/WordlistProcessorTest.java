package org.vorin.bestwords;

import org.junit.Test;
import org.vorin.bestwords.model.Wordlist;

public class WordlistProcessorTest {

    @Test
    public void testCombineMeanings() {
        var wordlist = new Wordlist();


//        var wordlists = Map.of(
////                Sources.WORD_REFERENCE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordlist.xml")),
//                Sources.GOOGLE_TRANSLATE_SOURCE, Wordlist.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordlist.xml")),
//                Sources.LINGUEE_SOURCE, Wordlist.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordlist.xml")));
//
//        int countLimit = 5;
//        for (var t : w.getTranslations()) {
//            t.setMeanings(new ArrayList<>());
//            WordlistProcessor.combineMeanings(t, wordlists);
//            if (countLimit-- == 0) break;
//        }

    }

}