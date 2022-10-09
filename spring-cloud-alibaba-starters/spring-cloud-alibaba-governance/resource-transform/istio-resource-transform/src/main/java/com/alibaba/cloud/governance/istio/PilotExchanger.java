/*
 * Copyright 2013-2018 the original author or authors.
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

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
public class PilotExchanger {

	private static final Logger log = LoggerFactory.getLogger(PilotExchanger.class);

	private LdsProtocol ldsProtocol;

	private CdsProtocol cdsProtocol;

	private EdsProtocol edsProtocol;

	private RdsProtocol rdsProtocol;

	private void observeListeners(List<Listener> listeners) {
		if (listeners == null) {
			return;
		}
		synchronized (ldsProtocol) {
			ldsProtocol.resolveAuthRules(listeners);
			Set<String> resourceName = ldsProtocol.getRouteNames(listeners);
			if (resourceName != null && !resourceName.isEmpty()) {
				rdsProtocol.observeResource(resourceName, this::observeRoutes);
			}
		}
	}

	private void observeClusters(List<Cluster> clusters) {
		synchronized (cdsProtocol) {
			Set<String> resourceName = cdsProtocol.getEndPointNames(clusters);
			if (resourceName != null && !resourceName.isEmpty()) {
				// eds
				edsProtocol.observeResource(resourceName, this::observeEndpoints);
			}
			else {
				// lds
				ldsProtocol.observeResource(null, this::observeListeners);
			}
		}
	}

	private void observeEndpoints(List<ClusterLoadAssignment> endpoints) {
		synchronized (edsProtocol) {
			ldsProtocol.observeResource(null, this::observeListeners);
		}
	}

	private void observeRoutes(List<RouteConfiguration> routes) {
		synchronized (rdsProtocol) {
			rdsProtocol.resolveLabelRouting(routes);
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
