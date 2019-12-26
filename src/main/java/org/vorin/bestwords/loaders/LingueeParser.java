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

/**
 * https://try.jsoup.org/~LGB7rk_atM2roavV0d-czMt3J_g
 */
public class LingueeParser implements TranslationDataParser {

    private static final Pattern MEANING_PATTERN = Pattern.compile("<a.+?>(.+?)\\s?(<span.*)?<\\/a>");

    private static final Logger LOG = Logger.get(LingueeParser.class);

    @Override
    public void parseAndPublish(String foreignWord,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        Document doc = Jsoup.parse(translationData, StandardCharsets.UTF_8.name(), "");

        Elements rows = doc.select("div.translation_lines > div.translation");;

        var addedMeaninigs = new HashSet<String>();
        var sentences = new ArrayList<String>();
        var iter = rows.iterator();
        while (iter.hasNext()) {
            var row = iter.next();
            var meaningElem = row.select("h3.translation_desc > span.tag_trans > a.dictLink[id~=dictEntry]");
            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.toString());
            String meaning = null;
            if (matcher.find()) {
                meaning = matcher.group(1);
            }
            else {
                throw new RuntimeException(format("Couldn't match the meaning, meaningElem=[%s]", meaningElem));
            }

            var foreignSentenceRows = row.select("div.example_lines > div.example  > span.tag_e > span.tag_s");
            var translatedSentenceRows = row.select("div.example_lines > div.example  > span.tag_e > span.tag_t");
            if (foreignSentenceRows.size() != translatedSentenceRows.size()) {
                throw new RuntimeException(format("Unexpected scenario foreignSentenceRows.size()[%s] != translatedSentenceRows.size()[%s]",
                                                  foreignSentenceRows.size(), translatedSentenceRows.size()));
            }
            for (int i = 0; i < foreignSentenceRows.size(); i++) {
                sentences.add(Util.trimAndStripTrailingDot(foreignSentenceRows.get(i).text()) + " - " +
                              Util.trimAndStripTrailingDot(translatedSentenceRows.get(i).text()));
            }

            if (!addedMeaninigs.contains(meaning)) {
                translationPublisher.addMeaning(foreignWord, meaning, LINGUEE_SOURCE);
                addedMeaninigs.add(meaning);
                if (!sentences.isEmpty()) {
                    translationPublisher.addExampleSentence(foreignWord, meaning, Util.chooseShortestString(sentences), LINGUEE_SOURCE);
                }
            }
            sentences.clear();
        }
    }

}
