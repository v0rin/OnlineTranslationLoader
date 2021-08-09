package org.vorin.bestwords;

import org.junit.Ignore;
import org.junit.Test;
import org.vorin.bestwords.proxy.DefaultProxyProvider;
import org.vorin.bestwords.proxy.SimpleProxyTester;
import org.vorin.bestwords.util.Logger;

import java.io.IOException;
import java.util.function.Predicate;

public class DefaultProxyProviderTest {

    private static final Logger LOG = Logger.get(DefaultProxyProviderTest.class);

    @Ignore
    @Test
    public void defaultProxyProviderTest() throws IOException {

        String url = "https://www.linguee.com/english-spanish/search?source=english&query=carpet";
        var proxyProvider = new DefaultProxyProvider();

        Predicate<String> responseTester = response -> {
            if (response.contains("carpet") && response.contains("alfombra")) {
                return true;
            }
            else {
                return false;
            }
        };

        var proxy = proxyProvider.getNextWorkingProxy(new SimpleProxyTester(url, "carpet"));
        LOG.info("first working proxy found - " + proxy);

        var proxy2 = proxyProvider.getNextWorkingProxy(new SimpleProxyTester(url, "carpet"));
        LOG.info("second working proxy found - " + proxy2);
    }
}