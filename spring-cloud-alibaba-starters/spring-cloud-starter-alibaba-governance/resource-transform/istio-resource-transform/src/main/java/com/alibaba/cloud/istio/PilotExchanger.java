package com.alibaba.cloud.istio;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import com.alibaba.cloud.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.istio.util.XdsParseUtil;

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
