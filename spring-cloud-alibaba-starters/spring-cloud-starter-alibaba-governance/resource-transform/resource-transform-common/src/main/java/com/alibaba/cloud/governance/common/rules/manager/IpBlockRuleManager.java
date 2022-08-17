package com.alibaba.cloud.governance.common.rules.manager;

import com.alibaba.cloud.governance.common.rules.AndRule;
import com.alibaba.cloud.governance.common.rules.auth.IpBlockRule;
import com.alibaba.cloud.governance.common.rules.util.IpUtil;
import io.envoyproxy.envoy.config.core.v3.CidrRange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IpBlockRuleManager {
    private static Map<String, IpBlockRule> allowIpBlockRules = new ConcurrentHashMap<>();
    private static Map<String, IpBlockRule> denyIpBlockRules = new ConcurrentHashMap<>();

    public static boolean isValid(String sourceIp, String destIp, String remoteIp) {
        if (!denyIpBlockRules.isEmpty() && judgeIpBlockRule(denyIpBlockRules, sourceIp, destIp, remoteIp)) {
            return false;
        }
        if (allowIpBlockRules.isEmpty()) {
            return true;
        }
        return judgeIpBlockRule(allowIpBlockRules, sourceIp, destIp, remoteIp);
    }

    private static boolean judgeIpBlockRule(Map<String, IpBlockRule> rule, String sourceIp, String destIp, String remoteIp) {
        return rule.values().stream().allMatch(andRules -> judgeSourceIp(sourceIp, andRules) && judgeDestIp(destIp, andRules) && judgeRemoteIp(remoteIp, andRules));
    }

    private static boolean judgeSourceIp(String ip, IpBlockRule andRules) {
        return andRules.getSourceIps() == null || andRules.getSourceIps().isEmpty() || andRules.getSourceIps().getRules().stream().allMatch(orRules -> {
            boolean flag = orRules.getRules().stream().anyMatch(httpSourceIp -> IpUtil.matchIp(ip, httpSourceIp));
            return orRules.isNot() != flag;
        });
    }

    private static boolean judgeDestIp(String destIp, IpBlockRule andRules) {
        return andRules.getDestIps() == null || andRules.getDestIps().isEmpty() || andRules.getDestIps().getRules().stream().allMatch(orRules -> {
            boolean flag = orRules.getRules().stream().anyMatch(httpSourceIp -> IpUtil.matchIp(destIp, httpSourceIp));
            return orRules.isNot() != flag;
        });
    }

    private static boolean judgeRemoteIp(String remoteIp, IpBlockRule andRules) {
        return andRules.getRemoteIps() == null || andRules.getRemoteIps().isEmpty() || andRules.getRemoteIps().getRules().stream().allMatch(orRules -> {
            boolean flag = orRules.getRules().stream().anyMatch(httpSourceIp -> IpUtil.matchIp(remoteIp, httpSourceIp));
            return orRules.isNot() != flag;
        });
    }

    public static void addIpBlockRules(IpBlockRule rule, boolean isAllow) {
        if (isAllow) {
            allowIpBlockRules.put(rule.getName(), rule);
        } else {
            denyIpBlockRules.put(rule.getName(), rule);
        }
    }

    public static void updateDestIpRules(String name, AndRule<CidrRange> destIpRules, boolean isAllow) {
        if (isAllow && allowIpBlockRules.containsKey(name)) {
            allowIpBlockRules.get(name).setDestIps(destIpRules);
        }
        if (!isAllow && denyIpBlockRules.containsKey(name)) {
            denyIpBlockRules.get(name).setDestIps(destIpRules);
        }
    }

    public static void clear() {
        allowIpBlockRules.clear();
        denyIpBlockRules.clear();
    }
}
