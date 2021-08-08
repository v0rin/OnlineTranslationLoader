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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.vorin.bestwords.util.Sources.COLLINS_SOURCE;

/**
 * https://try.jsoup.org/~LGB7rk_atM2roavV0d-czMt3J_g
 */
public class CollinsSentencesParser implements TranslationDataParser {

    private static final Pattern MEANING_PATTERN = Pattern.compile("(.*)");

    private static final Logger LOG = Logger.get(CollinsSentencesParser.class);

    private final int sentencePreferablyNotShorterThan;

    public CollinsSentencesParser(int sentencePreferablyNotShorterThan) {
        this.sentencePreferablyNotShorterThan = sentencePreferablyNotShorterThan;
    }

    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        Document doc = Jsoup.parse(translationData, StandardCharsets.UTF_8.name(), "");

        Elements rows = doc.select("div.hom > div.sense");

        var iter = rows.iterator();
        while (iter.hasNext()) {
            var row = iter.next();
            var meaningElem = row.select("div.sense > span.type-translation").first();
            if (meaningElem == null) {
                continue;
            }
            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.text());
            String meaning;
            if (matcher.find()) {
                meaning = matcher.group(1);
            }
            else {
                throw new RuntimeException(format("Couldn't match the meaning, meaningElem=[%s]", meaningElem));
            }

            if (meaning.equals(wordInfo.getWordMeaning()) ||
                    meaning.equals("to " + wordInfo.getWordMeaning()) ||
                    meaning.equals(wordInfo.getWordMeaning() + "s")) {

                var sentences = new ArrayList<String>();
                var examples = row.select("div.type-example");
                for (var example : examples) {
                    var foreignSentenceElem = example.select("div.type-example > span.quote").first();
                    var translatedSentenceElem = example.select("div.type-example > span.type-translation").first();
                    sentences.add(Util.createExampleSentence(foreignSentenceElem.text(), translatedSentenceElem.text()));
                }

                if (!sentences.isEmpty()) {
                    // TODO wordType
                    translationPublisher.addMeaning(wordInfo.getForeignWord(), wordInfo.getWordMeaning(), "not-implemented", COLLINS_SOURCE);
                    translationPublisher.addExampleSentence(wordInfo.getForeignWord(),
                                                            wordInfo.getWordMeaning(),
                                                            "not-implemented",
                                                            Util.chooseShortestString(sentences, sentencePreferablyNotShorterThan),
                                                            COLLINS_SOURCE);
                }
                break;
            }
        }
    }

    @Override
    public String getSource() {
        return COLLINS_SOURCE;
    }
}
