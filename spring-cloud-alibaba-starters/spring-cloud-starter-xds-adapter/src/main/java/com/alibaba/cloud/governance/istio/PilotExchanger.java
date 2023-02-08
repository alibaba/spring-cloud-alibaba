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

package com.alibaba.cloud.governance.istio;

import java.util.List;
import java.util.Set;

import com.alibaba.cloud.governance.istio.protocol.impl.CdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.EdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.RdsProtocol;
import io.envoyproxy.envoy.config.cluster.v3.Cluster;
import io.envoyproxy.envoy.config.endpoint.v3.ClusterLoadAssignment;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.CollectionUtils;

/**
 * PilotExchanger is the class which communicate with istio pilot.
 *
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class PilotExchanger {

	private static final Logger log = LoggerFactory.getLogger(PilotExchanger.class);

	private final LdsProtocol ldsProtocol;

	private final CdsProtocol cdsProtocol;

	private final EdsProtocol edsProtocol;

	private final RdsProtocol rdsProtocol;

	private void observeListeners(List<Listener> listeners) {
		if (CollectionUtils.isEmpty(listeners)) {
			return;
		}
		Set<String> resourceName = ldsProtocol.getResourceNames();
		if (!CollectionUtils.isEmpty(resourceName)) {
			rdsProtocol.observeResource(resourceName, this::observeRoutes);
		}

	}

	private void observeClusters(List<Cluster> clusters) {
		Set<String> resourceName = cdsProtocol.getResourceNames();
		if (!CollectionUtils.isEmpty(resourceName)) {
			// eds
			edsProtocol.observeResource(resourceName, this::observeEndpoints);
		}
		else {
			// lds
			ldsProtocol.observeResource(null, this::observeListeners);
		}

	}

	private void observeEndpoints(List<ClusterLoadAssignment> endpoints) {
		ldsProtocol.observeResource(null, this::observeListeners);

	}

	private void observeRoutes(List<RouteConfiguration> routes) {
		if (log.isDebugEnabled()) {
			log.debug("A Xds configuration update is finished");
		}
	}

	public PilotExchanger(LdsProtocol ldsProtocol, CdsProtocol cdsProtocol,
			EdsProtocol edsProtocol, RdsProtocol rdsProtocol) {
		this.ldsProtocol = ldsProtocol;
		this.cdsProtocol = cdsProtocol;
		this.edsProtocol = edsProtocol;
		this.rdsProtocol = rdsProtocol;
		// observe cluster first, and update the other xds sequentially
		this.ldsProtocol.setNeedPolling(false);
		this.edsProtocol.setNeedPolling(false);
		this.rdsProtocol.setNeedPolling(false);
		// only polling cds, other protocol will be obtained sequentially
		this.cdsProtocol.setNeedPolling(true);
		cdsProtocol.observeResource(null, this::observeClusters);
	}

}
