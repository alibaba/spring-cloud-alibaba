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

package com.alibaba.cloud.governance.istio.constant;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public final class IstioConstants {

	/**
	 * Suffix of node.
	 */
	public static final String SVC_CLUSTER_LOCAL = ".svc.cluster.local";

	/**
	 * Default pod name.
	 */
	public static final String DEFAULT_POD_NAME = "sidecar";

	/**
	 * Default namespace name.
	 */
	public static final String DEFAULT_NAMESPACE = "default";

	/**
	 * Key of pod name.
	 */
	public static final String POD_NAME = "POD_NAME";

	/**
	 * Key of namespace name.
	 */
	public static final String NAMESPACE_NAME = "NAMESPACE_NAME";

	/**
	 * third-part jwt token location.
	 */
	public static final String THIRD_PART_JWT_PATH = "/var/run/secrets/tokens/istio-token";

	/**
	 * url of cds request.
	 */
	public static final String CDS_URL = "type.googleapis.com/envoy.config.cluster.v3.Cluster";

	/**
	 * url of eds request.
	 */
	public static final String EDS_URL = "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment";

	/**
	 * url of lds request.
	 */
	public static final String LDS_URL = "type.googleapis.com/envoy.config.listener.v3.Listener";

	/**
	 * url of rds request.
	 */
	public static final String RDS_URL = "type.googleapis.com/envoy.config.route.v3.RouteConfiguration";

	/**
	 * secure port of istiod.
	 */
	public static final int ISTIOD_SECURE_PORT = 15012;

	/**
	 * default polling size of xds request.
	 */
	public static final int DEFAULT_POLLING_SIZE = 10;

	/**
	 * default polling time of xds request.
	 */
	public static final int DEFAULT_POLLING_TIME = 30;

	/**
	 * default ip address of istiod.
	 */
	public static final String DEFAULT_ISTIOD_ADDR = "127.0.0.1";

	private IstioConstants() {

	}

}
