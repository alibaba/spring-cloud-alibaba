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
import java.util.List;

import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

/**
 * TODO: Fetch all endpoints in EdsProtocol.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class EdsProtocol extends AbstractXdsProtocol<ClusterLoadAssignment> {

	public EdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			XdsConfigProperties xdsConfigProperties) {
		super(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties);
	}

	@Override
	protected List<ClusterLoadAssignment> decodeXdsResponse(DiscoveryResponse response) {
		List<ClusterLoadAssignment> endpoints = new ArrayList<>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				ClusterLoadAssignment endpoint = res.unpack(ClusterLoadAssignment.class);
				endpoints.add(endpoint);
			}
			catch (Exception e) {
				log.error("Unpack cluster failed", e);
			}
		}
		fireXdsFilters(endpoints);
		return endpoints;
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.EDS_URL;
	}

}
