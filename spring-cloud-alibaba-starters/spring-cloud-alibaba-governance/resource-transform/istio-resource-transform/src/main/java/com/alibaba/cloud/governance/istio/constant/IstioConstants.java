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

package com.alibaba.cloud.governance.istio.constant;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
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
	 * jwt token location.
	 */
	public static final String KUBERNETES_SA_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

	private IstioConstants() {

	}

}
