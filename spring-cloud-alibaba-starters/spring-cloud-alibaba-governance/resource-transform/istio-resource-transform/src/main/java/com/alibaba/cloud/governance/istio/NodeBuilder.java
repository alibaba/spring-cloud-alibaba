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

import java.net.InetAddress;
import java.net.UnknownHostException;

import io.envoyproxy.envoy.config.core.v3.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeBuilder {

	private static final Logger log = LoggerFactory.getLogger(NodeBuilder.class);

	private static final String SVC_CLUSTER_LOCAL = ".svc.cluster.local";

	private static final String DEFAULT_POD_NAME = "sidecar";

	private static final String DEFAULT_NAMESPACE = "default";

	private static final String DEFAULT_SVC_NAME = "default";

	private static Node NODE;

	private NodeBuilder() {

	}

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
