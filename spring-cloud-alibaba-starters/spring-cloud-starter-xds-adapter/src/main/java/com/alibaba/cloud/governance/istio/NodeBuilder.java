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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.envoyproxy.envoy.config.core.v3.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public final class NodeBuilder {

	private static final Logger log = LoggerFactory.getLogger(NodeBuilder.class);

	private static Node NODE;

	private NodeBuilder() {

	}

	public static Node getNode() {
		try {
			if (NODE != null) {
				return NODE;
			}
			String podName = System.getenv(IstioConstants.POD_NAME);
			if (podName == null) {
				podName = IstioConstants.DEFAULT_POD_NAME;
			}
			String podNamespace = System.getenv(IstioConstants.NAMESPACE_NAME);
			if (podNamespace == null) {
				podNamespace = IstioConstants.DEFAULT_NAMESPACE;
			}
			String ip = "127.0.0.1";
			try {
				InetAddress local = InetAddress.getLocalHost();
				ip = local.getHostAddress();
			}
			catch (UnknownHostException e) {
				log.error("Can not get local ip", e);
			}
			Struct.Builder metaBuilder = Struct.newBuilder();
			// metadata is necessary for RequestAuthentication CRD
			metaBuilder.putFields("NAMESPACE",
					Value.newBuilder().setStringValue(podNamespace).build());
			NODE = Node.newBuilder()
					.setId(String.format(
							"sidecar~%s~%s.%s~%s" + IstioConstants.SVC_CLUSTER_LOCAL, ip,
							podName, podNamespace, podNamespace))
					.setMetadata(metaBuilder.build()).build();
			return NODE;
		}
		catch (Exception e) {
			log.error("Unable to create node for xds request", e);
		}
		return null;
	}

}
