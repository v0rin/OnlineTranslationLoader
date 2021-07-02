package org.vorin.bestwords.loaders;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.model.WordList;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.LangUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.vorin.bestwords.util.Sources.GOOGLE_TRANSLATE_SOURCE;

public class WordTypeTest {

    @Test
    public void google() throws IOException {
        var downloader = new GoogleTranslateDownloader(Dictionary.ES_EN);
        var parser = new GoogleTranslateParser(0.01, 5);
        var words = List.of("escuchó", "querían", "tenía").stream().map(word -> new WordInfo(word, null)).collect(toList());

        var publisher = new ConsoleOutTranslationPublisher();
        for (var word : words) {
            parser.parseAndPublish(word, downloader.download(word.getForeignWord()), publisher);
        };

    }

    @Test
    public void linguee() throws IOException {
        var downloader = new LingueeDownloader(Dictionary.ES_EN);
        var parser = new LingueeParser(LangUtil::sanitizeSpanishMeaning);
        var words = List.of("coche", "querían", "tenía").stream().map(word -> new WordInfo(word, null)).collect(toList());

        var publisher = new ConsoleOutTranslationPublisher();
        for (var word : words) {
            parser.parseAndPublish(word, downloader.download(word.getForeignWord()), publisher);
        };

        publisher.writeToTarget();
    }

}