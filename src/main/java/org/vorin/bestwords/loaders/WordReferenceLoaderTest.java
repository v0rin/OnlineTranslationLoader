package org.vorin.bestwords.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vorin.bestwords.AppConfig;
import org.vorin.bestwords.util.Logger;

public class WordReferenceLoaderTest {
    private static final Logger LOG = Logger.get(WordReferenceLoaderTest.class);

    private static final String URL_EN_ES = "https://www.wordreference.com/es/translation.asp?tranword=";
    private static final String GOOGLE_URL_EN_ES = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=";

    // https://www.wordreference.com/es/translation.asp?tranword=take
    // https://jsoup.org/cookbook/introduction/parsing-a-document
    // https://jsoup.org/apidocs/overview-summary.html

    public static void main(String[] args) throws Exception {
        Document doc = Jsoup.parse(new File(AppConfig.RES_DIR + "wordreference-translation-take.html"), StandardCharsets.UTF_8.name(), "https://www.wordreference.com");
        Elements rows = doc.select("table.WRD").select("tr[class~=even|odd|wrtopsection]");
        var iter = rows.iterator();
        iter.next(); // skip the header
        var row = iter.next();
        while (iter.hasNext()) {
            if (row.hasClass("wrtopsection")) {
                break;
            }
            if (row.id().contains("enes:")) {
                LOG.info("##############");
                printRow("1 - ", row);
                row = iter.next();
            }
            while (iter.hasNext() && !row.id().contains("enes:")) {
                printRow("2 - ", row);
                row = iter.next();
            }
        }

    }

    // <td class="ToWrd">agarrar<a title="conjugate agarrar" class="conjugate" href="/conj/EsVerbs.aspx?v=agarrar">⇒</a> <em class="tooltip POS2">vtr<span><i>verbo transitivo</i>: Verbo que requiere de un objeto directo ("[b]di[/b] la verdad", "[b]encontré[/b] una moneda").</span></em></td>
    private static void printRow(String prefix, Element row) {
        var cols = row.select("td.ToWrd");
        if (!cols.isEmpty()) {
            LOG.info(prefix + "meaning=" + cols.first().text());
        }
    }

    private static void old() throws Exception {
        Connection conn = jsoupConnection(URL_EN_ES + URLEncoder.encode("take", StandardCharsets.UTF_8));
        Document doc = conn.get();
        Elements elems = doc.select("table.WRD");
        Element elem = elems.select("tr.even").first();
        LOG.info(elem.html());

    }

    private static Connection jsoupConnection(String url) {
        return Jsoup.connect(url).followRedirects(true).ignoreContentType(true).ignoreHttpErrors(true)
                    .timeout(3000)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8")
                    .header("Connection", "keep-alive")
                    .header("Host", "www.wordreference.com")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Cache-Control", "max-age=0")
                    .header("Remote Address", "10.7.154.254:8080")
                    .header("Referrer Policy", "no-referrer-when-downgrade")
                    .header("Cookie", "crfgL0cSt0r=true; per_Mc_x=79c01df3e31346819cb3396b55f6e6e8; _ga=GA1.2.1660090343.1576680075; euconsent=BOrxkoOOrxkoOABABAENABAAAAAeZ7_______9______9uz_Gv_v_f__33e8__9v_l_7_-___u_-33d4-_1vX99yfm1-7ftr1tp386ues2LDiCA; per24h=1; _gid=GA1.2.709976004.1577121955; llang=enesi; per24c=6; per_M_12=26");
    }

}