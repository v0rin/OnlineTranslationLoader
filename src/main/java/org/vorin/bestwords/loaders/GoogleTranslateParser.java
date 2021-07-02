package org.vorin.bestwords.loaders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

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

        List<Triple<Double, String, String>> meaningsWithScores = new ArrayList<>();
        var wordTypes = new StringJoiner(", ");
        for (int i = 0; i < node.get(1).size(); i++) {
            String wordType = Util.stripSurroundingQuotes(node.get(1).get(i).get(0).toString());
            wordTypes.add(wordType);
            for (int j = 0; j < node.get(1).get(i).get(2).size(); j++)
            {
                String meaning = stripSurroundingQuotes(node.get(1).get(i).get(2).get(j).get(0).toString());
                var scoreObj = node.get(1).get(i).get(2).get(j).get(3);
                if (scoreObj == null) continue;
                double score = Double.parseDouble(scoreObj.toString());
                meaningsWithScores.add(new ImmutableTriple<>(score, meaning, wordType));
            }
        }

        meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());
//        LOG.info(format("meanings for [%s] - %s - %s", wordInfo.getForeignWord(), meaningsWithScores.toString(), wordTypes));

        var addedMeanings = new HashSet<String>();
        for (var ms : meaningsWithScores) {
            String meaning = ms.getMiddle();
            double score = ms.getLeft();
            String wordType = ms.getRight();
            if (!addedMeanings.contains(meaning)) { // don't add duplicate meanings
                // there are 3 situations
                // 1. all meanings have scores < minScore -> add the first one and break
                // 2. some of the meanings have scores >= minScore -> add those and break
                // 3. maxMeaningCount or more have score >= minScore -> add only maxMeaningCount and break

                if ((score < minScore && addedMeanings.isEmpty()) ||
                        (score >= minScore && addedMeanings.size() < maxMeaningCount)) {
                    translationPublisher.addMeaning(wordInfo.getForeignWord(),
                            meaning,
                            wordType,
                            GOOGLE_TRANSLATE_SOURCE,
                            format("Google score=[%s]", score));
                    addedMeanings.add(meaning);
                }
                else {
                    break;
                }
            }
        }

    }

    @Override
    public String getSource() {
        return GOOGLE_TRANSLATE_SOURCE;
    }

}
