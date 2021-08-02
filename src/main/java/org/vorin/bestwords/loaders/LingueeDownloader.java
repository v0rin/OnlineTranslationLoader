package org.vorin.bestwords.loaders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.ProxyProvider;
import org.vorin.bestwords.model.Dictionary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class LingueeDownloader implements TranslationDataDownloader {

    private static final String URL_EN_ES = "https://www.linguee.com/english-spanish/search?source=english&query=";
    private static final String URL_ES_EN = "https://www.linguee.com/spanish-english/search?source=spanish&query=";
    private static final String URL_EN_PL = "https://www.linguee.com/english-polish/search?source=english&query=";
    private static final String URL_PL_EN = "https://www.linguee.com/polish-english/search?source=polish&query=";

    private String url;
    private Dictionary dictionary;

    public LingueeDownloader(Dictionary dictionary) {
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
        String fullUrl = this.url + URLEncoder.encode(word, StandardCharsets.ISO_8859_1);
        Document doc = Jsoup.connect(fullUrl).get();
//        Document doc;
//        try {
//            setProxy(ProxyProvider.getCurrProxy());
//            doc = Jsoup.connect(fullUrl).get();
//        }
//        catch (HttpStatusException e) {
//            setProxy(ProxyProvider.getNextWorkingProxyForUrl(fullUrl, response -> response.contains(word)));
//            doc = Jsoup.connect(fullUrl).get();
//        }
        Elements rows = doc.select("div.isMainTerm > div.exact");
        if (rows.size() == 0) {
            rows = doc.select("div.isForeignTerm > div.exact");
        }
        String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
        return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
    }

    private void setProxy(ProxyProvider.ProxyHost proxy) {
        if (proxy != null) {
            System.setProperty("http.proxyHost", proxy.getHostName());
            System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
        }
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

}
