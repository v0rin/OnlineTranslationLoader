package org.vorin.bestwords.proxy;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import org.apache.http.HttpHost;

/**
 * Given the proxy and an url (args[0]) checks if the response's body contains a given word (args[1])
 */
public class SimpleProxyTester implements ProxyTester {

    private final String url;
    private final String wordToFindInBody;

    public SimpleProxyTester(String url, String wordToFindInBody) {
        this.url = url;
        this.wordToFindInBody = wordToFindInBody;
    }

    @Override
    public boolean test(ProxyHost proxy) {
        Unirest.setProxy(new HttpHost(proxy.getHostName(), proxy.getPort()));

        try {
            HttpResponse<String> response = Unirest.get(url).asString();
            if (response.getBody().contains(wordToFindInBody)) {
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
}
