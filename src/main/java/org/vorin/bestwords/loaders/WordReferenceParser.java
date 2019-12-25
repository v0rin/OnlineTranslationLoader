package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.vorin.bestwords.util.Sources.WORD_REFERENCE_SOURCE;
import static org.vorin.bestwords.util.Util.trimAndStripTrailingDot;

public class WordReferenceParser implements TranslationDataParser {

    private static final Logger LOG = Logger.get(WordReferenceParser.class);
    private static final Pattern MEANING_PATTERN = Pattern.compile("^<td class=\"ToWrd\">(.+?)<");

    enum RowType {
        MEANING, EXAMPLE_FOREIGN_SENTENCE, EXAMPLE_TRANSLATED_SENTENCE;
    }

    @Override
    public void parseAndPublish(String foreignWord,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        Document doc = Jsoup.parse(translationData, StandardCharsets.UTF_8.name(), "");

        Elements rows = doc.select("tr");;

        var meanings = new ArrayList<String>();
        var translatedSentences = new ArrayList<String>();
        var addedMeaninigs = new HashSet<String>();
        String foreignSentence = null;
        var iter = rows.iterator();
        boolean first = true;
        while (iter.hasNext()) {
            var row = iter.next();

            if (row.id().contains("enes:") || !row.select("td.wrtopsection").isEmpty()) {
                // sort out sentences - except the first one
                if (!first) {
                    for (int i = 0; i < Math.min(meanings.size(), translatedSentences.size()); i++) {
                        var meaning = meanings.get(i);
                        if (!translationPublisher.exampleSentenceExists(foreignWord, meaning)) {
                            String sentence = trimAndStripTrailingDot(foreignSentence) + " - " + trimAndStripTrailingDot(translatedSentences.get(i));
                            translationPublisher.addExampleSentence(foreignWord, meaning, sentence, WORD_REFERENCE_SOURCE);
                        }
                    }
                }
                first = false;

                meanings = new ArrayList<>();
                foreignSentence = null;
                translatedSentences = new ArrayList<>();
            }

            var rowType = getRowType(row);
            String value = getValueFromRow(row);
            if (value == null) {
                continue;
            }

            if (rowType == RowType.MEANING) {
                String[] meaningsArr = value.split(",");
                for (String meaning : meaningsArr) {
                    meaning = meaning.trim();
                    if (!addedMeaninigs.contains(meaning)) {
                        translationPublisher.addMeaning(foreignWord, meaning, WORD_REFERENCE_SOURCE);
                        addedMeaninigs.add(meaning);
                    }
                    meanings.add(meaning);
                }
            }
            else if (rowType == RowType.EXAMPLE_FOREIGN_SENTENCE) {
                foreignSentence = value;
            }
            else if (rowType == RowType.EXAMPLE_TRANSLATED_SENTENCE) {
                translatedSentences.add(value);
            }
            else {
                throw new RuntimeException(format("Unexpected scenario, value=%s, rowType=%s, row=%s", value, rowType, row));
            }
        }

    }

    private RowType getRowType(Element row) {
        var meaningElem = row.select("td.ToWrd").first();
        if (meaningElem != null) {
            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.toString());
            if (matcher.find()) {
                return RowType.MEANING;
            }
        }

        var foreignSentenceElem = row.select("td.FrEx").first();
        if (foreignSentenceElem != null) {
            return RowType.EXAMPLE_FOREIGN_SENTENCE;
        }

        var translatedSentenceElem = row.select("td.ToEx").first();
        if (translatedSentenceElem != null) {
            return RowType.EXAMPLE_TRANSLATED_SENTENCE;
        }

        return null;
    }

    private String getValueFromRow(Element row) {
        var meaningElem = row.select("td.ToWrd").first();
        if (meaningElem != null) {
            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.toString());
            if (matcher.find()) {
                return matcher.group(1);
            }
        }

        var foreignSentenceElem = row.select("td.FrEx").first();
        if (foreignSentenceElem != null) {
            return foreignSentenceElem.text();
        }

        var translatedSentenceElem = row.select("td.ToEx").first();
        if (translatedSentenceElem != null) {
            return translatedSentenceElem.text();
        }

        return null;
    }

}
