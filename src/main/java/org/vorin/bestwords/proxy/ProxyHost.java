package org.vorin.bestwords.proxy;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProxyHost {
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
