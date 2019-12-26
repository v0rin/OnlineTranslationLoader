package org.vorin.bestwords.loaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
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

    public GoogleTranslateParser(double minScore,
                                 int maxMeaningCount) {

        this.minScore = minScore;
        this.maxMeaningCount = maxMeaningCount;
    }

    @Override
    public void parseAndPublish(WordInfo wordInfo,
                                InputStream translationData,
                                TranslationPublisher translationPublisher) throws IOException {

        JsonNode node = OBJECT_MAPPER.readTree(translationData);
        translationData.close();

        List<Pair<Double, String>> meaningsWithScores = new ArrayList<>();
        for (int i = 0; i < node.get(1).size(); i++) {
            String wordType = node.get(1).get(i).get(0).toString();
            for (int j = 0; j < node.get(1).get(i).get(2).size(); j++)
            {
                String meaning = stripSurroundingQuotes(node.get(1).get(i).get(2).get(j).get(0).toString());
                var scoreObj = node.get(1).get(i).get(2).get(j).get(3);
                if (scoreObj == null) continue;
                double score = Double.parseDouble(scoreObj.toString());

                if (score >= minScore) {
                    meaningsWithScores.add(new ImmutablePair<>(score, meaning));
//                        LOG.info(format("possible meaning from [%s]: foreignWord=%s, meaning=%s; wordType=%s",
//                                        GOOGLE_TRANSLATE_SOURCE, word, meaning, wordType));
                }
            }
        }

        meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());
        LOG.info(format("meanings for [%s] - %s", wordInfo.getForeignWord(), meaningsWithScores.toString()));

        var addedMeanings = new HashSet<String>();
        for (var ms : meaningsWithScores) {
            String meaning = ms.getRight();
            if (!addedMeanings.contains(meaning)) { // don't add duplicate meanings
                translationPublisher.addMeaning(wordInfo.getForeignWord(), meaning, GOOGLE_TRANSLATE_SOURCE);
                addedMeanings.add(meaning);
                if (addedMeanings.size() >= maxMeaningCount) break;
            }
        }

    }

    @Override
    public String getSource() {
        return GOOGLE_TRANSLATE_SOURCE;
    }

}
