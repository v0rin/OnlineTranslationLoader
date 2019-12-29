package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.util.Dictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SynonimNetDownloader implements TranslationDataDownloader {

    private static final String URL = "https://synonim.net/synonim/";

    @Override
    public InputStream download(String word) throws IOException {
        String fullUrl = URL + word.replaceAll("\\s", "+");
        Document doc = Jsoup.connect(fullUrl).get();
        Elements rows = doc.select("div#mall > span > ul");
        String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
        return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Dictionary getDictionary() {
        return null;
    }
}
