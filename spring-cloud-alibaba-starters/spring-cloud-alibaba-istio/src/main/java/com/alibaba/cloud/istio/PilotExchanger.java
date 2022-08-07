package com.alibaba.cloud.istio;

import com.alibaba.cloud.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.istio.util.XdsParseUtil;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;

import java.util.*;

/**
 * @author musi
 * 实现对pilot的监听，定时拉取配置
 */
public class PilotExchanger {
    private LdsProtocol ldsProtocol;

    private void observeListeners(List<Listener> listeners) {
        XdsParseUtil.clearLdsCache();
        XdsParseUtil.resolveAuthRules(listeners);
    }

    public PilotExchanger(LdsProtocol ldsProtocol) {
        ldsProtocol.observeResource(null, this::observeListeners);
    }
}
