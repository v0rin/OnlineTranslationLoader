package org.vorin.bestwords;

import org.junit.Test;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Sources;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.*;
import static org.vorin.bestwords.AppConfig.RES_DIR;

public class WordListProcessorTest {

    @Test
    public void testCombineMeanings() {
        var wordlist = new WordList();


//        var wordlists = Map.of(
////                Sources.WORD_REFERENCE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-WordReferenceWordList.xml")),
//                Sources.GOOGLE_TRANSLATE_SOURCE, WordList.loadFromXml( new File(RES_DIR + DICT.name() + "-GoogleTranslateWordList.xml")),
//                Sources.LINGUEE_SOURCE, WordList.loadFromXml( new File( RES_DIR + DICT.name() + "-LingueeWordList.xml")));
//
//        int countLimit = 5;
//        for (var t : w.getTranslations()) {
//            t.setMeanings(new ArrayList<>());
//            WordListProcessor.combineMeanings(t, wordlists);
//            if (countLimit-- == 0) break;
//        }

    }

}