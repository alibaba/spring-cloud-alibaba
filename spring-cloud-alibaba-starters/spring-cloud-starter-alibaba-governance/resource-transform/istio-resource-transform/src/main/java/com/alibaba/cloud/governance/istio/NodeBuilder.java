package com.alibaba.cloud.governance.istio;

import io.envoyproxy.envoy.config.core.v3.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeBuilder {

	private static final Logger log = LoggerFactory.getLogger(NodeBuilder.class);

	private static final String SVC_CLUSTER_LOCAL = ".svc.cluster.local";

	private static final String DEFAULT_POD_NAME = "sidecar";

	private static final String DEFAULT_NAMESPACE = "default";

	private static final String DEFAULT_SVC_NAME = "default";

	private static Node NODE;

	public static Node getNode() {
		if (NODE != null) {
			return NODE;
		}
		String podName = System.getenv("POD_NAME");
		if (podName == null) {
			podName = DEFAULT_POD_NAME;
		}
		String podNamespace = System.getenv("NAMESPACE_NAME");
		if (podNamespace == null) {
			podNamespace = DEFAULT_NAMESPACE;
		}
		String svcName = System.getenv("SVC_NAME");
		if (svcName == null) {
			svcName = DEFAULT_SVC_NAME;
		}
		String ip = "127.0.0.1";
		try {
			InetAddress local = InetAddress.getLocalHost();
			ip = local.getHostAddress();
		}
		catch (UnknownHostException e) {
			log.error("can not get local ip", e);
		}
		return Node.newBuilder()
				.setId(String.format("sidecar~%s~%s~%s" + SVC_CLUSTER_LOCAL, ip, podName,
						podNamespace))
				.setCluster(svcName).build();
	}

}
