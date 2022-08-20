package com.alibaba.cloud.istio.rules.manager;

import com.alibaba.cloud.istio.rules.auth.IpBlockRule;
import com.alibaba.cloud.istio.util.IpUtil;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
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

    public static boolean judgeIpBlockRule(Map<String, IpBlockRule> rule, String sourceIp, String destIp, String remoteIp) {
        return rule.values().stream().allMatch(andRules -> (StringUtils.isEmpty(sourceIp) || andRules.getSourceIps().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpSourceIp -> IpUtil.matchIp(sourceIp, httpSourceIp));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        })) && (StringUtils.isEmpty(destIp) || andRules.getDestIps().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpDestIp -> IpUtil.matchIp(destIp, httpDestIp));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        })) && (StringUtils.isEmpty(remoteIp) || andRules.getRemoteIps().stream().allMatch(orRules -> {
            boolean flag = orRules.getLeft().stream().anyMatch(httpRemoteIp -> IpUtil.matchIp(remoteIp, httpRemoteIp));
            if (orRules.getRight()) {
                flag = !flag;
            }
            return flag;
        })));
    }

    public static void addIpBlockRules(IpBlockRule rule, boolean isAllow) {
        if (isAllow) {
            allowIpBlockRules.put(rule.getName(), rule);
        } else {
            denyIpBlockRules.put(rule.getName(), rule);
        }
    }

    public static void updateDestIpRules(String name, List<Pair<List<CidrRange>, Boolean>> destIpRules, boolean isAllow) {
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
