package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.util.Dictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.vorin.bestwords.util.Dictionary.EN_ES;

public class CollinsDownloader implements TranslationDataDownloader {

    private static final String URL_ES_EN = "https://www.collinsdictionary.com/dictionary/spanish-english/";

    private String url;

    public CollinsDownloader(Dictionary dictionary) {
        switch (dictionary) {
            case ES_EN: url = URL_ES_EN; break;
            case EN_ES: throw new RuntimeException("Unsupported translation - " + EN_ES);
        }
    }

    @Override
    public InputStream download(String word) throws IOException {
        Document doc = Jsoup.connect(url + URLEncoder.encode(word, StandardCharsets.UTF_8)).get();
        Elements rows = doc.select("div.content.definitions.dictionary > div.hom");
        String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
        return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
    }

}
