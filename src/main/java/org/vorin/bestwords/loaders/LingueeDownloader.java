package org.vorin.bestwords.loaders;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.model.Dictionary;
import org.vorin.bestwords.proxy.ProxyHost;
import org.vorin.bestwords.proxy.ProxyProvider;
import org.vorin.bestwords.proxy.ProxyTester;
import org.vorin.bestwords.proxy.SimpleProxyTester;

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
    private final ProxyProvider proxyProvider;
    private ProxyHost currProxy;
    private ProxyTester proxyTester = new SimpleProxyTester(URL_EN_ES + "carpet", "carpet");

    public LingueeDownloader(Dictionary dictionary) {
        this(dictionary, null);
    }

    public LingueeDownloader(Dictionary dictionary, ProxyProvider proxyProvider) {
        this.dictionary = dictionary;
        this.proxyProvider = proxyProvider;
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
        Document doc = jsoupGet(fullUrl, word);
        Elements rows = doc.select("div.isMainTerm > div.exact");
        if (rows.size() == 0) {
            rows = doc.select("div.isForeignTerm > div.exact");
        }
        String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
        return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
    }

    private Document jsoupGet(String url, String word) throws IOException {
        if (proxyProvider != null) {
            if (currProxy == null) {
                currProxy = proxyProvider.getNextWorkingProxy(proxyTester);
            }
            try {
                return Jsoup.connect(url).proxy(currProxy.getHostName(), currProxy.getPort()).get();
            } catch (HttpStatusException e) {
                currProxy = proxyProvider.getNextWorkingProxy(proxyTester);
                return Jsoup.connect(url).proxy(currProxy.getHostName(), currProxy.getPort()).get();
            }
        } else {
            return Jsoup.connect(url).get();
        }
    }

    @Override
    public Dictionary getDictionary() {
        return dictionary;
    }

}
