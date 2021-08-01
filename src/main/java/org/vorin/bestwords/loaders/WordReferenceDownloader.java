package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.model.Dictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WordReferenceDownloader implements TranslationDataDownloader {

    private static final String URL_EN_ES = "https://www.wordreference.com/es/translation.asp?tranword=";
    private static final String URL_ES_EN = "https://www.wordreference.com/es/en/translation.asp?spen=";
    private static final String URL_EN_PL = "https://www.wordreference.com/enpl/";
    private static final String URL_PL_EN = "https://www.wordreference.com/plen/";

    private String url;
    private Dictionary dictionary;

    public WordReferenceDownloader(Dictionary dictionary) {
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
        Document doc = Jsoup.connect(url + URLEncoder.encode(word, StandardCharsets.UTF_8)).get();
        Elements rows = doc.select("table.WRD").select("tr[class~=even|odd|wrtopsection]");
        var iter = rows.iterator();
        // skip and remove the first wrtopsection
        iter.next();
        iter.remove();
        boolean startRemoving = false;
        while (iter.hasNext()) {
            var row = iter.next();

            if (!startRemoving && row.hasClass("wrtopsection")) {
                startRemoving = true;
            }

            if (startRemoving) {
                iter.remove();
            }
        }

        String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
        return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

}
