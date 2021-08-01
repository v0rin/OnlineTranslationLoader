package org.vorin.bestwords.loaders;

import org.junit.Test;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.util.LangUtil;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class WordTypeTest {

//    @Ignore
    @Test
    public void google() throws IOException {
        var downloader = new GoogleTranslateDownloader(Dictionary.ES_EN);
        var parser = new GoogleTranslateParser(0.01, 5);
        var words = List.of("querían").stream().map(word -> new WordInfo(word, null)).collect(toList());
//        var words = List.of("escuchó", "querían", "tenía").stream().map(word -> new WordInfo(word, null)).collect(toList());

        var publisher = new ConsoleOutTranslationPublisher();
        for (var word : words) {
            parser.parseAndPublish(word, downloader.download(word.getForeignWord()), publisher);
        };

    }

//    @Ignore
    @Test
    public void linguee() throws IOException {
        var downloader = new LingueeDownloader(Dictionary.ES_EN);
        var parser = new LingueeParser(LangUtil::sanitizeSpanishMeaning);
        var words = List.of("querían").stream().map(word -> new WordInfo(word, null)).collect(toList());
//        var words = List.of("coche", "querían", "tenía").stream().map(word -> new WordInfo(word, null)).collect(toList());

        var publisher = new ConsoleOutTranslationPublisher();
        for (var word : words) {
            parser.parseAndPublish(word, downloader.download(word.getForeignWord()), publisher);
        };

        publisher.writeToTarget();
    }

}