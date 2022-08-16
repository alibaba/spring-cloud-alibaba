package com.alibaba.cloud.governance.istio;

import com.alibaba.cloud.governance.istio.util.XdsParseUtil;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;

import java.util.*;

/**
 * @author musi
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
