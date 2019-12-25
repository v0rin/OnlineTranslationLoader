package org.vorin.bestwords.loaders;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.util.Logger;

public class WordReferenceLoaderTest {
    private static final Logger LOG = Logger.get(WordReferenceLoaderTest.class);

    private static final Pattern MEANING_PATTERN = Pattern.compile("^<td class=\"ToWrd\">(.+?)<");

    private static final String URL_EN_ES = "https://www.wordreference.com/es/translation.asp?tranword=";
    private static final String URL_ES_EN = "https://www.wordreference.com/es/en/translation.asp?spen=";
    private static final String SOURCE = "wordreference";


    @Test
    public void test() throws Exception {
        Document doc = Jsoup.parse(new File(AppConfig.RES_DIR + "wordreference-translation-take.html"), StandardCharsets.UTF_8.name(), "");
        Elements rows = doc.select("table.WRD").select("tr[class~=even|odd|wrtopsection]");
        var iter = rows.iterator();
        iter.next(); // skip the first wrtopsection
        while (iter.hasNext()) {
            var row = iter.next();

            if (row.hasClass("wrtopsection")) {
                break;
            }

            if (row.id().contains("enes:")) {
                LOG.info("##############");
                printRow("", row);
                continue;
            }

            printRow("", row);
        }

    }

    private void printRow(String prefix, Element row) {
        var meaningElem = row.select("td.ToWrd").first();
        if (meaningElem != null) {
            Matcher matcher = MEANING_PATTERN.matcher(meaningElem.toString());
            if (matcher.find()) {
                LOG.info(prefix + "meaning=" + matcher.group(1));
                return;
            }
        }

        var foreignSentenceElem = row.select("td.FrEx").first();
        if (foreignSentenceElem != null) {
            LOG.info(prefix + "example sentence=" + foreignSentenceElem.text());
            return;
        }

        var translatedSentenceElem = row.select("td.ToEx").first();
        if (translatedSentenceElem != null) {
            LOG.info(prefix + "example sentence translation=" + translatedSentenceElem.text());
            return;
        }

    }

    @Test
    public void old() throws Exception {
        Connection conn = jsoupConnection(URL_EN_ES + URLEncoder.encode("take", StandardCharsets.UTF_8));
        Document doc = conn.get();
        Elements elems = doc.select("table.WRD");
        Element elem = elems.select("tr.even").first();
        LOG.info(elem.html());
    }

    private static Connection jsoupConnection(String url) {
        return Jsoup.connect(url);
//        .followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
//                    .timeout(3000)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
//                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                    .header("Accept-Encoding", "gzip, deflate, br")
//                    .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8")
//                    .header("Connection", "keep-alive")
//                    .header("Host", "www.wordreference.com")
//                    .header("Sec-Fetch-Mode", "navigate")
//                    .header("Sec-Fetch-Site", "none")
//                    .header("Sec-Fetch-User", "?1")
//                    .header("Upgrade-Insecure-Requests", "1")
//                    .header("Cache-Control", "max-age=0")
//                    .header("Remote Address", "10.7.154.254:8080")
//                    .header("Referrer Policy", "no-referrer-when-downgrade")
//                    .header("Cookie", "crfgL0cSt0r=true; per_Mc_x=79c01df3e31346819cb3396b55f6e6e8; _ga=GA1.2.1660090343.1576680075; euconsent=BOrxkoOOrxkoOABABAENABAAAAAeZ7_______9______9uz_Gv_v_f__33e8__9v_l_7_-___u_-33d4-_1vX99yfm1-7ftr1tp386ues2LDiCA; per24h=1; _gid=GA1.2.709976004.1577121955; llang=enesi; per24c=6; per_M_12=26");
    }

}