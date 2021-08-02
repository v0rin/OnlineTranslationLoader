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
import java.util.function.Function;
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

    private final Function<String, String> meaningSanitizer;
    private final int maxMeaningCount;

    public LingueeParser(Function<String, String> meaningSanitizer, int maxMeaningCount) {
        this.meaningSanitizer = meaningSanitizer;
        this.maxMeaningCount = maxMeaningCount;
    }

    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        Document doc = Jsoup.parse(translationData, StandardCharsets.UTF_8.name(), "");

        Elements rows = doc.select("div.translation_lines > div.translation, div.translation_lines > div.translation_group > div.translation_group_line");
        for (var iter = rows.iterator(); iter.hasNext();) {
            if (!iter.next().select("span.notascommon").isEmpty()) {
                iter.remove();
            }
        }
        rows = rows.select("div.translation");

        var sentences = new ArrayList<String>();
        int addedMeaningsCount = 0;
        for (var iter = rows.iterator(); iter.hasNext();) {
            var row = iter.next();
            var meaningElem = row.select(".translation_desc > span.tag_trans > a.dictLink[id~=dictEntry]");

            var wordTypeElem = row.select(".translation_desc > span.tag_trans > span.tag_type");
            if (wordTypeElem.size() > 1) {
                throw new RuntimeException("more than one word type in linguee: " + wordTypeElem);
            }
            String wordType = wordTypeElem.stream().map(e -> e.attributes().get("title").replaceAll(",\\p{Z}(feminine|masculine)", "")).findFirst().orElse("");

            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.toString());
            String meaning;
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
                sentences.add(Util.createExampleSentence(foreignSentenceRows.get(i).text(), translatedSentenceRows.get(i).text()));
            }

            meaning = meaningSanitizer.apply(meaning);

//                LOG.info(format("meanings for [%s] - %s - %s", wordInfo.getForeignWord(), meaning, wordType));
            translationPublisher.addMeaning(wordInfo.getForeignWord(), meaning, wordType, LINGUEE_SOURCE + "#" + (addedMeaningsCount+1));
            if (!sentences.isEmpty()) {
                translationPublisher.addExampleSentence(wordInfo.getForeignWord(), meaning, Util.chooseShortestString(sentences), LINGUEE_SOURCE);
            }
            sentences.clear();
            if (++addedMeaningsCount >= maxMeaningCount) {
                break;
            }
        }
    }

    @Override
    public String getSource() {
        return LINGUEE_SOURCE;
    }

}
