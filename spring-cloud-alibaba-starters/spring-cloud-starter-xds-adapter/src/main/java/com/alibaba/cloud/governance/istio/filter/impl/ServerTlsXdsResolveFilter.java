/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.governance.istio.filter.impl;

import java.util.List;

import com.alibaba.cloud.commons.governance.event.ServerProtoChangedEvent;
import com.alibaba.cloud.governance.istio.TlsContext;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.AbstractXdsResolveFilter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.FilterChainMatch;
import io.envoyproxy.envoy.config.listener.v3.Listener;

import org.springframework.util.CollectionUtils;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class ServerTlsXdsResolveFilter extends AbstractXdsResolveFilter<List<Listener>> {

	@Override
	public boolean resolve(List<Listener> listeners) {
		if (listeners == null || listeners.isEmpty()) {
			return false;
		}
		boolean tls = false;
		for (Listener listener : listeners) {
			List<FilterChain> filterChains = listener.getFilterChainsList();
			if (!VIRTUAL_INBOUND.equals(listener.getName())) {
				continue;
			}
			if (CollectionUtils.isEmpty(filterChains)) {
				continue;
			}
			for (FilterChain filterChain : filterChains) {
				if (!VIRTUAL_INBOUND.equals(filterChain.getName())) {
					continue;
				}
				FilterChainMatch match = filterChain.getFilterChainMatch();
				if (TLS.equals(match.getTransportProtocol())) {
					tls = true;
				}
				break;
			}
		}
		if (TlsContext.isOnce()) {
			applicationContext.publishEvent(new ServerProtoChangedEvent(this, tls));
		}
		else {
			// The tls context will only be modified once while initializing.
			TlsContext.setIsTls(tls);
		}
		return true;
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.LDS_URL;
	}

}
