package org.vorin.bestwords;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import static java.lang.String.format;

import static org.vorin.bestwords.Util.print;
import static org.vorin.bestwords.Util.sleep;
import static org.vorin.bestwords.Util.stripSurroundingQuotes;

public class GoogleTranslateLoader {

    private static final long SLEEP_BETWEEN_REQUESTS_MS = 5000;

    //https://stackoverflow.com/questions/8085743/google-translate-vs-translate-api
    //https://stackoverflow.com/questions/57397073/difference-between-the-google-translate-api
    private static final String GOOGLE_TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    private boolean testMode;
    private ObjectMapper objectMapper = new ObjectMapper();

    public GoogleTranslateLoader(boolean testMode) {
		this.testMode = testMode;
    }

    public Map<String, List<String>> load(List<String> words) throws IOException {
        print("GoogleTranslateLoader starting...");
        var translations = new HashMap<String, List<String>>();
        for (String word : words) {
            JsonNode node = objectMapper.readTree(getJsonForWord(word));

            for (int i = 0; i < node.get(1).size(); i++) {
                String wordType = node.get(1).get(i).get(0).toString();
                for (int j = 0; j < node.get(1).get(i).get(2).size(); j++)
                {
                    String meaning = stripSurroundingQuotes(node.get(1).get(i).get(2).get(j).get(0).toString());
                    double score = Double.parseDouble(node.get(1).get(i).get(2).get(j).get(3).toString());
                    if (score >= 0.01) {
                        var meanings = translations.computeIfAbsent(word, w -> new ArrayList<>());
                        meanings.add(meaning);
                        print("added " + meaning + " - " + Double.toString(score) + " - " + wordType);
                    }
                    else {
                        print("skipped " + meaning + " - " + Double.toString(score) + " - " + wordType);
                    }

                }
            }

            if (!testMode) sleep(SLEEP_BETWEEN_REQUESTS_MS); // wait before making another request to Google
        }
        print("GoogleTranslateLoader loading complete");
        return translations;
    }

    private InputStream getJsonForWord(String word) throws IOException {
        if (testMode) {
            return new FileInputStream("CodeEnvy/res/translations-" + word + ".json");
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
