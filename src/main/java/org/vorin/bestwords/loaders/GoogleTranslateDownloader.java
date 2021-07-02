package org.vorin.bestwords.loaders;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.vorin.bestwords.util.Dictionary;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

public class GoogleTranslateDownloader implements TranslationDataDownloader {

    private static final String URL_EN_ES = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";
    private static final String URL_ES_EN = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=es&tl=en&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    private static final String URL_EN_PL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=pl&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";
    private static final String URL_PL_EN = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=pl&tl=en&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    private String url;
    private Dictionary dictionary;

    public GoogleTranslateDownloader(Dictionary dictionary) {
        this.dictionary = dictionary;
        switch (dictionary) {
            case EN_ES: url = URL_EN_ES; break;
            case ES_EN: url = URL_ES_EN; break;

            case EN_PL: url = URL_EN_PL; break;
            case PL_EN: url = URL_PL_EN; break;
        }
    }

    @Override
    public InputStream download(String word) throws IOException {
        HttpRequest request = Unirest.get(url + URLEncoder.encode(word, StandardCharsets.ISO_8859_1));
        try {
            HttpResponse<String> response = request.asString();
            return response.getRawBody();
        }
        catch (UnirestException e) {
            throw new IOException(format("Exception while parsing json for word %s", word), e);
        }
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

}
