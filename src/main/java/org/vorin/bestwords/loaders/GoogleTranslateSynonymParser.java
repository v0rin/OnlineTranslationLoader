package org.vorin.bestwords.loaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.vorin.bestwords.util.Sources.GOOGLE_TRANSLATE_SOURCE;
import static org.vorin.bestwords.util.Util.stripSurroundingQuotes;


public class GoogleTranslateSynonymParser implements TranslationDataParser {

    private static final Logger LOG = Logger.get(GoogleTranslateParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {
        JsonNode node = OBJECT_MAPPER.readTree(translationData);
        translationData.close();

        int synonymCount = 0;
        for (var n1 : node.get(11)) {
            for (var n2 : n1.get(1)) {
                var synonym = stripSurroundingQuotes(n2.get(0).get(0).toString());
                // TODO wordType
                translationPublisher.addMeaning(wordInfo.getParsedForeignWord(), synonym, "not-implemented", GOOGLE_TRANSLATE_SOURCE);
                synonymCount++;
            }
        }
        LOG.info(format("added [%s] synonyms for [%s]", synonymCount, wordInfo.getParsedForeignWord()));
    }

    @Override
    public String getSource() {
        return GOOGLE_TRANSLATE_SOURCE;
    }

}
