package com.alibaba.cloud.governance.istio;

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

import java.util.List;
import java.util.Set;

/**
 * @author musi
 */
public class PilotExchanger {

	private static final Logger log = LoggerFactory.getLogger(PilotExchanger.class);

	private LdsProtocol ldsProtocol;

	private CdsProtocol cdsProtocol;

	private EdsProtocol edsProtocol;

	private RdsProtocol rdsProtocol;

	private void observeListeners(List<Listener> listeners) {
		synchronized (ldsProtocol) {
			ldsProtocol.clearCache();
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
			// TODO: update route info
		}
	}

	public PilotExchanger(LdsProtocol ldsProtocol, CdsProtocol cdsProtocol,
			EdsProtocol edsProtocol, RdsProtocol rdsProtocol) {
		this.ldsProtocol = ldsProtocol;
		this.cdsProtocol = cdsProtocol;
		this.edsProtocol = edsProtocol;
		this.rdsProtocol = rdsProtocol;
		// observe cluster first, and update the other xds sequentially
		cdsProtocol.observeResource(null, this::observeClusters);
	}

}
