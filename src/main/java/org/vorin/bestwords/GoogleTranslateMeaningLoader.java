package org.vorin.bestwords;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static org.vorin.bestwords.Util.sleep;
import static org.vorin.bestwords.Util.stripSurroundingQuotes;

public class GoogleTranslateMeaningLoader {

    private static final String WORKING_DIR = System.getProperty("user.dir");

    private static final Logger LOG = Logger.get(GoogleTranslateMeaningLoader.class);

    private static final long WAIT_BETWEEN_REQUESTS_MS = 5000;

    //https://stackoverflow.com/questions/8085743/google-translate-vs-translate-api
    //https://stackoverflow.com/questions/57397073/difference-between-the-google-translate-api
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    private ObjectMapper objectMapper = new ObjectMapper();

    private TranslationPublisher translationPublisher;
    private int maxMeaningCount;
    private double minScore;
    private boolean useCache;

    private Stopwatch googleRequestsStopwatch;

    public GoogleTranslateMeaningLoader(TranslationPublisher translationPublisher, double minScore, int maxMeaningCount, boolean useCache) {
        this.translationPublisher = translationPublisher;
        this.minScore = minScore;
        this.maxMeaningCount = maxMeaningCount;
        this.useCache = useCache;
    }

    public void load(List<String> words) throws IOException {
        LOG.info("started...");
        googleRequestsStopwatch = Stopwatch.createUnstarted();
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

            meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());
            LOG.info(format("meanings for [%s] - %s", word, meaningsWithScores.toString()));

            meaningsWithScores.stream()
                    .limit(Math.min(meaningsWithScores.size(), maxMeaningCount))
                    .forEachOrdered(ms -> {
                        translationPublisher.addTranslation(word, ms.getRight(), null, null);
                    });
        }
        LOG.info("loading complete");
    }

    private InputStream getJsonForWord(String word) throws IOException {
        if (useCache) {
            File jsonFile = getJsonFileForWord(word);
            if (jsonFile.exists()) {
                LOG.info("using the cached file for word=" + word);
                return new FileInputStream(jsonFile);
            }
            LOG.info(format("no cached file for word=%s asking Google...", word));
        }

        HttpRequest request = Unirest.get(GOOGLE_TRANSLATE_URL + word);
        try {

            while (googleRequestsStopwatch.isRunning() && googleRequestsStopwatch.elapsed().toMillis() < WAIT_BETWEEN_REQUESTS_MS) {
                sleep(WAIT_BETWEEN_REQUESTS_MS/50);
            }
            HttpResponse<String> response = request.asString();
            googleRequestsStopwatch.reset().start();

            File jsonFile = saveToJsonFile(word, response.getRawBody());
            return new FileInputStream(jsonFile);
        }
        catch (UnirestException e) {
            throw new IOException(format("Exception while parsing json for word %s", word), e);
        }
    }

    private File saveToJsonFile(String word, InputStream responseRawBody) throws IOException {
        File jsonFile = getJsonFileForWord(word);
        try(OutputStream fos = new FileOutputStream(jsonFile)){
            IOUtils.copy(responseRawBody, fos);
        }

        return jsonFile;
    }

    private File getJsonFileForWord(String word) {
        return new File(WORKING_DIR + "/res/GoogleTranslateMeaningLoaderCache/translation-" + word + ".json");
    }

}
