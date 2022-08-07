package com.alibaba.cloud.istio.util;

import com.alibaba.cloud.istio.rules.auth.IpBlockRule;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpUtil {
    private static final Logger log = LoggerFactory.getLogger(IpBlockRule.class);
    public static boolean matchIp(String remoteIp, CidrRange rule) {
        String remoteIpBinary = ip2BinaryString(remoteIp);
        if (StringUtils.isEmpty(remoteIpBinary)) {
            return false;
        }
        String prefix = rule.getAddressPrefix();
        String prefixBinary = ip2BinaryString(prefix);
        if (StringUtils.isEmpty(prefixBinary)) {
            return false;
        }
        if (!rule.hasPrefixLen()) {
            return remoteIpBinary.equals(prefixBinary);
        }
        int prefixLen = rule.getPrefixLen().getValue();
        if (remoteIpBinary.length() >= prefixLen && prefixBinary.length() >= prefixLen) {
            return remoteIpBinary.substring(0, prefixLen).equals(prefixBinary.substring(0, prefixLen));
        }
        return false;
    }

    /**
     *
     * @param ip dotted ip string, for example: 127.0.0.1
     * @return
     */
    public static String ip2BinaryString(String ip) {
        try {
            String[] ips = ip.split("\\.");
            return String.format("%32s", Long.toBinaryString(Long.parseLong(ips[0]) << 24) + (Long.parseLong(ips[1]) << 16) + (Long.parseLong(ips[2]) << 8) + Long.parseLong(ips[3])).replace(" ", "0");
        } catch (Exception e) {
            log.error("failed to parse ip {} to binary string", ip);
        }
        return "";
    }
}
