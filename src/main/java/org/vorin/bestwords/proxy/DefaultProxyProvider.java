package org.vorin.bestwords.proxy;

import org.vorin.bestwords.util.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class DefaultProxyProvider implements ProxyProvider {

    private static final Logger LOG = Logger.get(DefaultProxyProvider.class);

//    private static final List<ProxyHost> PROXY_LIST = Arrays.asList(
//            new ProxyHost("196.242.89.3", 10000),
//            new ProxyHost("196.242.89.3", 10001),
//            new ProxyHost("196.242.89.3", 10002),
//            new ProxyHost("196.242.89.3", 10003),
//            new ProxyHost("196.242.89.3", 10004),
//            new ProxyHost("196.242.89.3", 10005));
    private final List<ProxyHost> PROXY_LIST = IntStream.range(10_000, 11_000).boxed().map(port -> new ProxyHost("196.242.89.3", port)).collect(toList());

    private int currProxyIdx = 0;

    @Override
    public ProxyHost getCurrProxy() {
        return PROXY_LIST.get(currProxyIdx);
    }

    @Override
    public ProxyHost getNextWorkingProxy(ProxyTester tester) {
        int startingProxyIdx = currProxyIdx;
        boolean firstPass = true;

        while (firstPass || currProxyIdx != startingProxyIdx) {
            currProxyIdx = currProxyIdx < PROXY_LIST.size() - 1 ? ++currProxyIdx : 0;
            ProxyHost proxy = PROXY_LIST.get(currProxyIdx);
            LOG.info(format("checking proxy %s:%s ...", proxy.getHostName(), proxy.getPort()));
            if (tester.test(proxy)) {
                LOG.info(format("proxy %s:%s is working", proxy.getHostName(), proxy.getPort()));
                return proxy;
            }
            LOG.warn(format("proxy %s:%s is not working", proxy.getHostName(), proxy.getPort()));
            if (currProxyIdx == startingProxyIdx) { // means we already did one full pass through the proxy list
                firstPass = false;
            }
        }

        throw new RuntimeException("No proxy is working");
    }

}
