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
@Component
public class PilotExchanger {
    @Autowired
    private LdsProtocol ldsProtocol;

    List<TargetRules> targetRulesAllowed = new ArrayList<>();
    List<TargetRules> targetRulesDenied = new ArrayList<>();
    List<SourceRules> sourceRulesAllowed = new ArrayList<>();
    List<SourceRules> sourceRulesDenied = new ArrayList<>();

    private void observeListeners(List<Listener> listeners) {
        List<RBAC> rbacList = ListenerResolver.resolveRbac(listeners);
        for (RBAC rbac : rbacList) {
            for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
                // permission
                List<Permission> permissions = entry.getValue().getPermissionsList();
                for (Permission permission : permissions) {
                    switch (rbac.getAction()) {
                        case ALLOW:
                            this.targetRulesAllowed.add(new TargetRules(entry.getKey(), permission, true));
                            break;
                        case DENY:
                            this.targetRulesDenied.add(new TargetRules(entry.getKey(), permission, false));
                            break;
                        default:
                            break;
                    }
                }
                List<Principal> principals = entry.getValue().getPrincipalsList();
                for (Principal principal : principals) {
                    switch (rbac.getAction()) {
                        case ALLOW:
                            this.sourceRulesAllowed.add(new SourceRules(entry.getKey(), principal, true));
                            break;
                        case DENY:
                            this.sourceRulesDenied.add(new SourceRules(entry.getKey(), principal, false));
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public PilotExchanger() {
        ldsProtocol.observeResource(null, this::observeListeners);
    }
}
