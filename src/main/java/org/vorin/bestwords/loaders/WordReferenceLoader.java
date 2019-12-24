package org.vorin.bestwords.loaders;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.vorin.bestwords.util.Logger;
import org.vorin.bestwords.TranslationPublisher;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static org.vorin.bestwords.AppConfig.RES_DIR;
import static org.vorin.bestwords.util.Util.sleep;
import static org.vorin.bestwords.util.Util.stripSurroundingQuotes;

public class WordReferenceLoader {

    public enum Dictionary {
        EN_ES, ES_EN;
    }

    private static final Logger LOG = Logger.get(WordReferenceLoader.class);

    private static final long WAIT_BETWEEN_REQUESTS_MS = 5000;

    private static final String URL_EN_ES = "https://www.wordreference.com/es/translation.asp?tranword=";
    private static final String URL_ES_EN = "https://www.wordreference.com/es/en/translation.asp?spen=";
    private static final String SOURCE = "wordreference";

    private ObjectMapper objectMapper = new ObjectMapper();

    private String url;
    private TranslationPublisher translationPublisher;
    private int maxMeaningCount;
    private double minScore;
    private boolean useCache;

    private Stopwatch requestsStopwatch;

    public WordReferenceLoader(Dictionary dictionary,
                                        TranslationPublisher translationPublisher,
                                        double minScore,
                                        int maxMeaningCount,
                                        boolean useCache) {
        if (dictionary == Dictionary.EN_ES) {
            this.url = URL_EN_ES;
        } else if (dictionary == Dictionary.ES_EN) {
            this.url = URL_ES_EN;
        }
        this.translationPublisher = translationPublisher;
        this.minScore = minScore;
        this.maxMeaningCount = maxMeaningCount;
        this.useCache = useCache;
    }

    public void load(List<String> words) throws IOException {
        LOG.info("started...");
        requestsStopwatch = Stopwatch.createUnstarted();
        var addedForeignWords = new HashSet<String>();
        for (String word : words) {
            if (addedForeignWords.contains(word)) {
                throw new RuntimeException(format("foreignWord [%s] has been already added - there are some duplicated words it seems", word));
            }
            JsonNode node = objectMapper.readTree(getDataForWord(word));

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
//                                        SOURCE, word, meaning, wordType));
                    }
                }
            }

            meaningsWithScores = meaningsWithScores.stream().sorted((m1, m2) -> m2.getLeft().compareTo(m1.getLeft())).collect(toList());
            LOG.info(format("meanings for [%s] - %s", word, meaningsWithScores.toString()));

            var addedMeanings = new HashSet<String>();
            for (var ms : meaningsWithScores) {
                String meaning = ms.getRight();
                if (!addedMeanings.contains(meaning)) { // don't add duplicate meanings
                    translationPublisher.addMeaning(word, meaning, SOURCE);
                    addedMeanings.add(meaning);
                    if (addedMeanings.size() >= maxMeaningCount) break;
                }
            }
            addedForeignWords.add(word);
        }
        LOG.info("loading complete");
    }

    private InputStream getDataForWord(String word) throws IOException {
        if (useCache) {
            File dataFile = getDataFileForWord(word);
            if (dataFile.exists()) {
                LOG.info("using the cached file for word=" + word);
                return new FileInputStream(dataFile);
            }
            LOG.info(format("no cached file for word=%s making http request...", word));
        }

        return null;
        // try {

        //     while (requestsStopwatch.isRunning() && requestsStopwatch.elapsed().toMillis() < WAIT_BETWEEN_REQUESTS_MS) {
        //         sleep(WAIT_BETWEEN_REQUESTS_MS/50);
        //     }
            // HttpResponse<String> response = request.asString();
            // requestsStopwatch.reset().start();

            // File dataFile = saveToDataFile(word, response.getRawBody());
            // return new FileInputStream(dataFile);

        // }
        // catch (UnirestException e) {
        //     throw new IOException(format("Exception while parsing data for word %s", word), e);
        // }
    }

    private File saveToDataFile(String word, InputStream responseRawBody) throws IOException {
        File dataFile = getDataFileForWord(word);
        // TODO @af extract the interesting part and write to file
        try(OutputStream fos = new FileOutputStream(dataFile)){
            IOUtils.copy(responseRawBody, fos);
        }

        return dataFile;
    }

    private File getDataFileForWord(String word) {
        return new File(RES_DIR + "WordReferenceLoaderCache/translation-" + word);
    }

}
