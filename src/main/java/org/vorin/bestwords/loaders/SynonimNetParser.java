package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.vorin.bestwords.util.Sources.LINGUEE_SOURCE;
import static org.vorin.bestwords.util.Sources.SYNONIM_NET_SOURCE;

/**
 * https://try.jsoup.org/~LGB7rk_atM2roavV0d-czMt3J_g
 */
public class SynonimNetParser implements TranslationDataParser {

    private static final Logger LOG = Logger.get(SynonimNetParser.class);

    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        Document doc = Jsoup.parse(translationData, StandardCharsets.UTF_8.name(), "");

        Elements rows = doc.select("li > a");

        var iter = rows.iterator();
        int count = 0;
        while (iter.hasNext()) {
            var row = iter.next();
            translationPublisher.addMeaning(wordInfo.getParsedForeignWord(), row.text(), SYNONIM_NET_SOURCE);
            count++;
        }
        // LOG.info(format("added %s synonyms from [%s] for word [%s]", count, SYNONIM_NET_SOURCE, wordInfo.getParsedForeignWord()));
    }

    @Override
    public String getSource() {
        return SYNONIM_NET_SOURCE;
    }

}
