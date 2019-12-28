package org.vorin.bestwords;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.HttpHost;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ProxyProvider {
    // http://www.gatherproxy.com/
    // http://www.gatherproxy.com/proxylist/country/?c=Poland
    private static final List<ProxyHost> PROXY_LIST = Arrays.asList(
            new ProxyHost("178.128.50.214", 8080),
            new ProxyHost("69.63.73.171", 53281),
            new ProxyHost("83.175.166.234", 8080),
            new ProxyHost("176.103.45.24", 8080));

    private static int currProxyIdx = -1;

    public static ProxyHost getNextWorkingProxyForUrl(String url, Predicate<String> responseTester) {
        int startingProxyIdx = currProxyIdx == -1 ? 0 : currProxyIdx;
        boolean firstPass = true;

        while (firstPass || currProxyIdx != startingProxyIdx) {
            currProxyIdx = currProxyIdx < PROXY_LIST.size() - 1 ? ++currProxyIdx : 0;
            ProxyHost proxy = PROXY_LIST.get(currProxyIdx);
            if (isProxyWorking(proxy, url, responseTester)) {
                return proxy;
            }
            if (currProxyIdx == startingProxyIdx) { // means we already did one full pass through the proxy list
                firstPass = false;
            }
        }

        throw new RuntimeException("No proxy is working");
    }

    private static boolean isProxyWorking(ProxyHost proxy, String url, Predicate<String> responseTester) {
        Unirest.setProxy(new HttpHost(proxy.getHostName(), proxy.getPort()));

        try {
            HttpResponse<String> response = Unirest.get(url).asString();
            if (responseTester.test(response.getBody())) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
    }


    public static class ProxyHost {
        private String hostName;
        private int port;

        public ProxyHost(String hostName, int port) {
            this.hostName = hostName;
            this.port = port;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("hostName", hostName)
                    .append("port", port)
                    .toString();
        }

    }
}
