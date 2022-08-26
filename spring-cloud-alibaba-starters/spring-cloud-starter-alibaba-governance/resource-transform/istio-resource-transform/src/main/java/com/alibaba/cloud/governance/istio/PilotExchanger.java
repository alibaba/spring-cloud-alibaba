package com.alibaba.cloud.governance.istio;

import io.envoyproxy.envoy.config.listener.v3.Listener;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author musi
 */
public class PilotExchanger {

	private static final Logger log = LoggerFactory.getLogger(PilotExchanger.class);

	private final LdsProtocol ldsProtocol;

	private void observeListeners(List<Listener> listeners) {
		synchronized (ldsProtocol) {
			ldsProtocol.clearLdsCache();
			ldsProtocol.resolveAuthRules(listeners);
		}
	}

	public PilotExchanger(LdsProtocol ldsProtocol) {
		this.ldsProtocol = ldsProtocol;
		ldsProtocol.observeResource(null, this::observeListeners);
	}

}
