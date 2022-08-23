package com.alibaba.cloud.governance.istio;

import com.alibaba.cloud.governance.istio.util.XdsParseUtil;
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

	private LdsProtocol ldsProtocol;

	private void observeListeners(List<Listener> listeners) {
		log.info("fetching listeners from pilot!");
		XdsParseUtil.clearLdsCache();
		XdsParseUtil.resolveAuthRules(listeners);
	}

	public PilotExchanger(LdsProtocol ldsProtocol) {
		ldsProtocol.observeResource(null, this::observeListeners);
	}

}
