package org.vorin.bestwords;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import static org.vorin.bestwords.Util.print;

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

    public void load(String[] words) throws IOException {
        for (String word : words) {
            JsonNode node = objectMapper.readTree(getJsonForWord(word));

            for (int i = 0; i < node.get(1).size(); i++) {
                //print(node.get(1).get(i).toPrettyString());
                String wordType = node.get(1).get(i).get(0).toString();
                for (int j = 0; j < node.get(1).get(i).get(2).size(); j++)
                {
                    String translation = node.get(1).get(i).get(2).get(j).get(0).toString();
                    double score = Double.parseDouble(node.get(1).get(i).get(2).get(j).get(3).toString());
                    print(translation + " - " + Double.toString(score) + " - " + wordType);
                }
            }
            if (!testMode) Thread.sleep(SLEEP_BETWEEN_REQUESTS_MS); // wait before making another request to Google
        }
    }

    private InputStream getJsonForWord(String word) throws IOException {
        if (testMode) {

        }
//            JsonNode node = objectMapper.readTree(new File("CodeEnvy1/translations.json"));

        HttpRequest request = Unirest.get(GOOGLE_TRANSLATE_URL + word);
        try {
            Http<Response> response = request.asString();
			return response.getRawBody();
		} catch (UnirestException e) {
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, encoding);
			throw new IOException(format("Exception while parsing json for word %s. The response was [%s]", word, writer.toString()), e);
		}
    }

}
