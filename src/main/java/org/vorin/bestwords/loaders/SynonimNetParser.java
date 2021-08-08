package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
            // todo wordType if needed
            translationPublisher.addMeaning(wordInfo.getParsedForeignWord(), row.text(), "not-implemented", SYNONIM_NET_SOURCE);
            count++;
        }
        // LOG.info(format("added %s synonyms from [%s] for word [%s]", count, SYNONIM_NET_SOURCE, wordInfo.getParsedForeignWord()));
    }

    @Override
    public String getSource() {
        return SYNONIM_NET_SOURCE;
    }

}
