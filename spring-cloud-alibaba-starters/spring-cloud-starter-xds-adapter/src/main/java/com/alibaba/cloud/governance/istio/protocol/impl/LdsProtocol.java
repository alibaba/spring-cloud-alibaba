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

package com.alibaba.cloud.governance.istio.protocol.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.XdsResolveFilter;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

/**
 * LdsProtocol contains the authentication configuration and other configuration about
 * security.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class LdsProtocol extends AbstractXdsProtocol<Listener> {

	public LdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			XdsConfigProperties xdsConfigProperties,
			List<XdsResolveFilter<List<Listener>>> ldsFilters) {
		super(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties);
		// init filters
		for (XdsResolveFilter<List<Listener>> filter : ldsFilters) {
			if (IstioConstants.LDS_URL.equals(filter.getTypeUrl())) {
				filters.add(filter);
			}
		}
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.LDS_URL;
	}

	@Override
	public List<Listener> decodeXdsResponse(DiscoveryResponse response) {
		List<Listener> listeners = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				Listener listener = res.unpack(Listener.class);
				if (listener != null) {
					listeners.add(listener);
				}
			}
			catch (Exception e) {
				log.error("Unpack listeners failed", e);
			}
		}
		fireXdsFilters(listeners);
		return listeners;
	}

	@Override
	protected Set<String> resolveResourceNames(List<Listener> resources) {
		Set<String> routeNames = new HashSet<>();
		resources.forEach(listener -> routeNames.addAll(listener.getFilterChainsList()
				.stream().flatMap((e) -> e.getFiltersList().stream())
				.map(Filter::getTypedConfig).map(any -> {
					try {
						if (!any.is(HttpConnectionManager.class)) {
							return null;
						}
						return any.unpack(HttpConnectionManager.class);
					}
					catch (InvalidProtocolBufferException e) {
						return null;
					}
				}).filter(Objects::nonNull).map(HttpConnectionManager::getRds)
				.map(Rds::getRouteConfigName).filter(StringUtils::isNotEmpty)
				.collect(Collectors.toList())));
		return routeNames;

	}

}
