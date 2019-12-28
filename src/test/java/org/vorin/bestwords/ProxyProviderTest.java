package org.vorin.bestwords;

import org.junit.Test;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.util.function.Predicate;

public class ProxyProviderTest {

    private static final Logger LOG = Logger.get(ProxyProviderTest.class);

//    @Ignore
    @Test
    public void getNextWorkingProxyForUrl() throws IOException {

        String url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=es&hl=en&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&dt=gt&source=bh&ssel=0&tsel=0&kc=1&q=take";

        Predicate<String> responseTester = response -> {
            if (response.contains("take") && response.contains("tomar")) {
                return true;
            }
            else {
                return false;
            }
        };

        var proxy = ProxyProvider.getNextWorkingProxyForUrl(url, responseTester);
        LOG.info("first working proxy found - " + proxy);

        var proxy2 = ProxyProvider.getNextWorkingProxyForUrl(url, responseTester);
        LOG.info("second working proxy found - " + proxy2);
    }
}