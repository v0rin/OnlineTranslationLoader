package org.vorin.bestwords;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static org.vorin.bestwords.Util.sleep;
import static org.vorin.bestwords.Util.stripSurroundingQuotes;

public class GoogleTranslateMeaningLoader {

    private static final String WORKING_DIR = System.getProperty("user.dir");

    private static final Logger LOG = Logger.get(GoogleTranslateMeaningLoader.class);

    private static final long SLEEP_BETWEEN_REQUESTS_MS = 5000;

    //https://stackoverflow.com/questions/8085743/google-translate-vs-translate-api
    //https://stackoverflow.com/questions/57397073/difference-between-the-google-translate-api
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    private ObjectMapper objectMapper = new ObjectMapper();

    private TranslationPublisher translationPublisher;
    private int maxMeaningCount;
    private boolean testMode;
    private double minScore;

    public GoogleTranslateMeaningLoader(TranslationPublisher translationPublisher, double minScore, int maxMeaningCount, boolean testMode) {
        this.translationPublisher = translationPublisher;
        this.minScore = minScore;
        this.maxMeaningCount = maxMeaningCount;
        this.testMode = testMode;
    }

    public void load(List<String> words) throws IOException {
        LOG.info("started...");
        for (String word : words) {
            JsonNode node = objectMapper.readTree(getJsonForWord(word));

            List<Pair<Double, String>> meaningsWithScores = new ArrayList<>();
            for (int i = 0; i < node.get(1).size(); i++) {
                // String wordType = node.get(1).get(i).get(0).toString();
                for (int j = 0; j < node.get(1).get(i).get(2).size(); j++)
                {
                    String meaning = stripSurroundingQuotes(node.get(1).get(i).get(2).get(j).get(0).toString());
                    double score = Double.parseDouble(node.get(1).get(i).get(2).get(j).get(3).toString());

                    if (score >= minScore) {
                        meaningsWithScores.add(new ImmutablePair<>(score, meaning));
                    }
                }
            }

            LOG.info(meaningsWithScores.toString());

            meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());

            LOG.info(meaningsWithScores.toString());

            int loopCount = Math.min(meaningsWithScores.size(), maxMeaningCount);
            for (int i = 0; i < loopCount; i++) {
                translationPublisher.addTranslation(word, meaningsWithScores.get(i).getRight(), null, null);
            }

            if (!testMode) sleep(SLEEP_BETWEEN_REQUESTS_MS); // wait before making another request to Google
        }
        LOG.info("loading complete");
    }

    private InputStream getJsonForWord(String word) throws IOException {
        // TODO @af add cache - so I don't have to go to google each time, there can be a flag use cache, in case I would start pulling diff information
        if (testMode) {
            return new FileInputStream(WORKING_DIR + "/res/translations-" + word + ".json");
        }

        HttpRequest request = Unirest.get(GOOGLE_TRANSLATE_URL + word);
        try {
            HttpResponse<String> response = request.asString();
            return response.getRawBody();
        } catch (UnirestException e) {
            throw new IOException(format("Exception while parsing json for word %s", word), e);
        }
    }

}
