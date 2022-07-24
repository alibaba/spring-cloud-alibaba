package com.alibaba.cloud.istio;

import com.alibaba.cloud.istio.rules.SourceRules;
import com.alibaba.cloud.istio.rules.TargetRules;
import com.alibaba.cloud.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.istio.util.ListenerResolver;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.config.rbac.v3.Policy;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author musi
 * 实现对pilot的监听，定时拉取配置
 */
public class PilotExchanger {
    private LdsProtocol ldsProtocol;

    private List<TargetRules> targetRulesAllowed = new ArrayList<>();
    private List<TargetRules> targetRulesDenied = new ArrayList<>();
    private List<SourceRules> sourceRulesAllowed = new ArrayList<>();
    private List<SourceRules> sourceRulesDenied = new ArrayList<>();

    private void observeListeners(List<Listener> listeners) {
        List<RBAC> rbacList = ListenerResolver.resolveRbac(listeners);
        for (RBAC rbac : rbacList) {
            for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
                // permission
                List<Permission> permissions = entry.getValue().getPermissionsList();
                TargetRules targetRules;
                for (Permission permission : permissions) {
                    switch (rbac.getAction()) {
                        case ALLOW:
                        case UNRECOGNIZED:
                            targetRules = new TargetRules(entry.getKey(), permission, true);
                            if (!targetRules.isAny()) {
                                this.targetRulesAllowed.add(targetRules);
                            }
                            break;
                        case DENY:
                            targetRules = new TargetRules(entry.getKey(), permission, false);
                            if (!targetRules.isAny()) {
                                this.targetRulesDenied.add(targetRules);
                            }
                            break;
                        default:
                            break;
                    }
                }
                List<Principal> principals = entry.getValue().getPrincipalsList();
                SourceRules rules;
                for (Principal principal : principals) {
                    switch (rbac.getAction()) {
                        case ALLOW:
                        case UNRECOGNIZED:
                            rules = new SourceRules(entry.getKey(), principal, true);
                            if (!rules.isAny()) {
                                this.sourceRulesAllowed.add(rules);
                            }
                            break;
                        case DENY:
                            rules = new SourceRules(entry.getKey(), principal, false);
                            if (!rules.isAny()) {
                                this.sourceRulesDenied.add(rules);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public PilotExchanger(LdsProtocol ldsProtocol) {
        ldsProtocol.observeResource(null, this::observeListeners);
    }
}
