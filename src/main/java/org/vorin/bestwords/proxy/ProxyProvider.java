package org.vorin.bestwords.proxy;

public interface ProxyProvider {

    public ProxyHost getNextWorkingProxy(ProxyTester tester);

    public ProxyHost getCurrProxy();


}
