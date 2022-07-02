package com.alibaba.cloud.nacos.intetuntil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;

public class InetIPUtils implements Closeable {
    private final ExecutorService executorService;

    private final Log log = LogFactory.getLog(InetIPUtils.class);

    private final InetUtilsProperties properties;

    public InetIPUtils(final InetUtilsProperties properties) {
        this.properties = properties;
        this.executorService = Executors.newSingleThreadExecutor((r) -> {
            Thread thread = new Thread(r);
            thread.setName("spring.cloud.alibaba.inetutilsIPV6");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void close() {
        this.executorService.shutdown();
    }

    public InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
        InetAddress address = this.findFirstNonLoopbackAddress();
        if (address != null) {
            return this.convertAddress(address);
        } else {
            InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
            this.properties.setDefaultIpAddress("0:0:0:0:0:0:0:1");
            hostInfo.setHostname(this.properties.getDefaultHostname());
            hostInfo.setIpAddress(this.properties.getDefaultIpAddress());
            return hostInfo;
        }
    }

    public InetAddress findFirstNonLoopbackAddress() {
        InetAddress address = null;

        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    log.trace("Testing interface:" + ifc.getDisplayName());
                    if (ifc.getIndex() < lowest || address == null) {
                        lowest = ifc.getIndex();
                    }
                    else if (address != null) {
                        continue;
                    }

                    if (!ignoreInterface(ifc.getDisplayName())) {
                        for (Enumeration<InetAddress> addrs = ifc
                                .getInetAddresses(); addrs.hasMoreElements();) {
                            InetAddress inetAddress = addrs.nextElement();
                            if (inetAddress instanceof Inet6Address
                                    && !inetAddress.isLoopbackAddress()
                                    && isPreferredAddress(inetAddress)) {
                                this.log.trace("Found non-loopback interface: "
                                        + ifc.getDisplayName());
                                address = inetAddress;
                            }
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            log.error("Cannot get first non-loopback address", e);
        }

        if (address != null) {
            return address;
        }

        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            log.warn("Unable to retrieve localhost");
        }

        return null;
    }

    boolean isPreferredAddress(InetAddress address) {
        if (this.properties.isUseOnlySiteLocalInterfaces()) {
            final boolean siteLocalAddress = address.isSiteLocalAddress();
            if (!siteLocalAddress) {
                log.trace("Ignoring address"+address.getHostAddress());
            }
            return siteLocalAddress;
        }
        final List<String> preferredNetworks = this.properties.getPreferredNetworks();
        if (preferredNetworks.isEmpty()) {
            return true;
        }
        for (String regex : preferredNetworks) {
            final String hostAddress = address.getHostAddress();
            if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
                return true;
            }
        }
        log.trace("Ignoring address: " + address.getHostAddress());
        return false;
    }

    boolean ignoreInterface(String interfaceName) {
        for (String regex : this.properties.getIgnoredInterfaces()) {
            if (interfaceName.matches(regex)) {
                log.trace("Ignoring interface: " + interfaceName);
                return true;
            }
        }
        return false;
    }

    public InetUtils.HostInfo convertAddress(final InetAddress address) {
        InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
        Future<String> result = this.executorService.submit(address::getHostName);

        String hostname;
        try {
            hostname = result.get(this.properties.getTimeoutSeconds(), TimeUnit.SECONDS);
        }
        catch (Exception e) {
            log.info("Cannot determine local hostname");
            hostname = "localhost";
        }
        hostInfo.setHostname(hostname);
        hostInfo.setIpAddress(address.getHostAddress());
        return hostInfo;
    }
}
