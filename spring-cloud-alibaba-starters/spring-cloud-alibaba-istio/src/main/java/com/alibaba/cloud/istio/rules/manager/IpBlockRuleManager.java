package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.IpBlockRule;
import com.alibaba.cloud.istio.util.IpUtil;
import io.envoyproxy.envoy.config.core.v3.CidrRange;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IpBlockRuleManager {
    private static Map<String, IpBlockRule> ipBlockRules = new ConcurrentHashMap<>();
    private static Map<String, IpBlockRule> notIpBlockRules = new ConcurrentHashMap<>();
    public static boolean isValid(String remoteIp) {
        boolean isDeny = notIpBlockRules.values().stream().anyMatch(andRules -> andRules.getRemoteIps().stream().allMatch(orRules -> orRules.stream().anyMatch(cidrRange -> IpUtil.matchIp(remoteIp, cidrRange))));
        if (isDeny) {
            return false;
        }
        if (ipBlockRules.isEmpty()) {
            return true;
        }
        return ipBlockRules.values().stream().anyMatch(andRules -> andRules.getRemoteIps().stream().allMatch(orRules -> orRules.stream().anyMatch(cidrRange -> IpUtil.matchIp(remoteIp, cidrRange))));
    }

    public static void addIpBlockRules(IpBlockRule rule, boolean isAllow) {
        if (isAllow) {
            ipBlockRules.put(rule.getName(), rule);
        } else {
            notIpBlockRules.put(rule.getName(), rule);
        }
    }

    public static void updateDestIpRules(String name, List<List<CidrRange>> destIpRules, boolean isAllow) {
        if (isAllow && ipBlockRules.containsKey(name)) {
            ipBlockRules.get(name).setDestIps(destIpRules);
        }
        if (!isAllow && notIpBlockRules.containsKey(name)) {
            notIpBlockRules.get(name).setDestIps(destIpRules);
        }
    }

    public static void clear() {
        ipBlockRules.clear();
        notIpBlockRules.clear();
    }
}
