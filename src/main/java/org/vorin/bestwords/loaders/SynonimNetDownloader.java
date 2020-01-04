package org.vorin.bestwords.loaders;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.vorin.bestwords.WordListProcessor;
import org.vorin.bestwords.util.Dictionary;
import org.vorin.bestwords.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.lang.String.format;

public class SynonimNetDownloader implements TranslationDataDownloader {
    private static final Logger LOG = Logger.get(SynonimNetDownloader.class);

    private static final String URL = "https://synonim.net/synonim/";

    @Override
    public InputStream download(String word) throws IOException {
        String fullUrl = URL + word.replaceAll("\\s", "+");
        try {
            Document doc = Jsoup.connect(fullUrl)
                    .header("cookie", "PHPSESSID=95c41eeb587f241dfe078f35b8029481; _cmpQcif3pcsupported=1; googlepersonalization=OsTOTjOsTOTjgA; eupubconsent=BOsTOTjOsTOTjAKAbAENAAAAgAAAAA; euconsent=BOsTOTjOsTOTjAKAbBENC0-AAAAtFr_7__7-_9_-_f__9uj3Or_v_f__30ccL59v_h_7v-_5fi_20nV4u_1vft9yfk1-5ctDztp505iakivHmqNeb9v_mz3_5pxP78k89r7337Ew_v8_v-b7JCON_A; sas_euconsent=BOsTOTjOsTOTjAKAbBENC0-AAAAtFr_7__7-_9_-_f__9uj3Or_v_f__30ccL59v_h_7v-_5fi_20nV4u_1vft9yfk1-5ctDztp505iakivHmqNeb9v_mz3_5pxP78k89r7337Ew_v8_v-b7JCON_A; _ga=GA1.2.2013808831.1577623250; _gid=GA1.2.3751285.1577623251; __gads=ID=cd7bd57863c7c13f:T=1577623251:S=ALNI_MZU2O8t_394p9Kp4p1NyzH0F3UyBQ; auth=9b2c81be33f237c6f7b5869ba1558560; _gat_gtag_UA_5420901_23=1")
                    .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
                    .header("accept-language", "en-GB,en;q=0.9,pl-PL;q=0.8,pl;q=0.7,en-US;q=0.6")
                    .header("accept-encoding", "gzip, deflate, br")
                    .header("authority", "synonim.net")
                    .header("scheme", "https")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36")
                    .get();
            boolean noSynonyms = doc.outerHtml().contains("Nie znaleziono synonim");
            Elements rows = doc.select("div#mall > span > ul");
            if (noSynonyms) {
                LOG.info(format("no synonyms for [%s]", word));
            }
            else if (rows.outerHtml().isBlank()) {
                throw new RuntimeException("response empty - probably some issues with too many requests");
            }
            String output = "<html><body><table>\n" + rows.outerHtml() + "\n</table></body></html>";
            return new ByteArrayInputStream(output.getBytes(StandardCharsets.UTF_8));
        }
        catch (HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                return new ByteArrayInputStream("<html><body><table></table></body></html>".getBytes(StandardCharsets.UTF_8));
            }
            else {
                throw e;
            }
        }
    }

    @Override
    public Dictionary getDictionary() {
        return Dictionary.EN_PL;
    }
}
