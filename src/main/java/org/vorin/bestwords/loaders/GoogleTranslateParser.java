package org.vorin.bestwords.loaders;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.vorin.bestwords.util.Sources.GOOGLE_TRANSLATE_SOURCE;
import static org.vorin.bestwords.util.Util.stripSurroundingQuotes;

public class GoogleTranslateParser implements TranslationDataParser {

    private static final Logger LOG = Logger.get(GoogleTranslateParser.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final double minScore;
    private final int maxMeaningCount;
    private final int nodeIdx;
    private final boolean parsePhrase;

    public GoogleTranslateParser(double minScore,
                                 int maxMeaningCount) {
        this(minScore, maxMeaningCount, false);
    }

    public GoogleTranslateParser(double minScore,
                                 int maxMeaningCount,
                                 boolean parsePhrase) {
        this.minScore = minScore;
        this.maxMeaningCount = maxMeaningCount;
        this.parsePhrase = parsePhrase;
        if (parsePhrase) {
            this.nodeIdx = 5;
        } else {
            this.nodeIdx = 1;
        }
    }


    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {

        JsonNode node;
        try {
            node = OBJECT_MAPPER.readTree(translationData);
        } catch (JsonParseException e) {
            if (e.getMessage().contains("Unexpected character ('<' (code 60))")) {
                LOG.info("del src\\main\\resources\\caches\\google-translate-cache-EN_ES\\" + wordInfo.getForeignWord());
            }
            throw e;
        }
        translationData.close();

        List<Triple<Double, String, String>> meaningsWithScores = new ArrayList<>();
        for (int i = 0; i < node.get(nodeIdx).size(); i++) {
            String wordType = Util.stripSurroundingQuotes(node.get(nodeIdx).get(i).get(0).toString());
            for (int j = 0; j < node.get(nodeIdx).get(i).get(2).size(); j++)
            {
                String meaning = stripSurroundingQuotes(node.get(nodeIdx).get(i).get(2).get(j).get(0).toString());
                if (parsePhrase) {
                    meaningsWithScores.add(new ImmutableTriple<>(1.0, meaning, "phrase"));
                    break;
                }
                var scoreObj = node.get(nodeIdx).get(i).get(2).get(j).get(3);
                if (scoreObj == null || scoreObj.getNodeType().equals(JsonNodeType.NULL)) continue;
                double score = Double.parseDouble(scoreObj.toString());
                meaningsWithScores.add(new ImmutableTriple<>(score, meaning, wordType));
            }
        }

        meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());
//        LOG.info(format("meanings for [%s] - %s - %s", wordInfo.getForeignWord(), meaningsWithScores.toString(), wordTypes));

        int addedMeaningsCount = 0;
        for (var ms : meaningsWithScores) {
            String meaning = ms.getMiddle();
            double score = ms.getLeft();
            String wordType = ms.getRight();
            // there are 3 situations
            // 1. all meanings have scores < minScore -> add the first one and break
            // 2. some of the meanings have scores >= minScore -> add those and break
            // 3. maxMeaningCount or more have score >= minScore -> add only maxMeaningCount and break

            if ((score < minScore && addedMeaningsCount == 0) ||
                    (score >= minScore && addedMeaningsCount < maxMeaningCount)) {
                translationPublisher.addMeaning(wordInfo.getForeignWord(),
                        meaning,
                        wordType,
                        format("%s#%s(%.2f)", GOOGLE_TRANSLATE_SOURCE, (addedMeaningsCount+1), score),
                        "");
                addedMeaningsCount++;
            }
            else {
                break;
            }
        }
    }

    @Override
    public String getSource() {
        return GOOGLE_TRANSLATE_SOURCE;
    }

}
