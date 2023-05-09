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
	 * Pod namespace key.
	 */
	public static final String POD_NAMESPACE = "POD_NAMESPACE";

	/**
	 * Workload namespace key.
	 */
	public static final String WORKLOAD_NAMESPACE = "WORKLOAD_NAMESPACE";

	/**
	 * First part jwt of Istio.
	 */
	public final static String FIRST_PARTY_JWT = "first-party-jwt";

	/**
	 * Third part jwt of Istio.
	 */
	public final static String THIRD_PARTY_JWT = "third-party-jwt";

	/**
	 * First-part jst token location.
	 */
	public static final String FIRST_PART_JWT_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

	/**
	 * Third-part jwt token location.
	 */
	public static final String THIRD_PART_JWT_PATH = "/var/run/secrets/tokens/istio-token";

	/**
	 * Url of cds request.
	 */
	public static final String CDS_URL = "type.googleapis.com/envoy.config.cluster.v3.Cluster";

	/**
	 * Url of eds request.
	 */
	public static final String EDS_URL = "type.googleapis.com/envoy.config.endpoint.v3.ClusterLoadAssignment";

	/**
	 * Url of lds request.
	 */
	public static final String LDS_URL = "type.googleapis.com/envoy.config.listener.v3.Listener";

	/**
	 * Url of rds request.
	 */
	public static final String RDS_URL = "type.googleapis.com/envoy.config.route.v3.RouteConfiguration";

	/**
	 * Url of sds request.
	 */
	public static final String SDS_URL = "type.googleapis.com/envoy.extensions.transport_sockets.tls.v3.Secret";

	/**
	 * Secure port of istiod.
	 */
	public static final int ISTIOD_SECURE_PORT = 15012;

	/**
	 * Default polling size of xds request.
	 */
	public static final int DEFAULT_POLLING_SIZE = 10;

	/**
	 * Default polling time of xds request.
	 */
	public static final int DEFAULT_POLLING_TIME = 30;

	/**
	 * Default ip address of istiod.
	 */
	public static final String DEFAULT_ISTIOD_ADDR = "127.0.0.1";

	/**
	 * Default ca addr of istiod.
	 */
	public final static String DEFAULT_CA_ADDR = "istiod.istio-system.svc:15012";

	/**
	 * Default trust domain.
	 */
	public final static String DEFAULT_TRUST_DOMAIN = "cluster.local";

	/**
	 * K8s namespace path.
	 */
	public final static String KUBERNETES_NAMESPACE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";

	/**
	 * RSA key.
	 */
	public final static String RSA_KEY_SIZE_KEY = "RSA_KEY_SIZE";

	/**
	 * Default RSA key size.
	 */
	public final static String DEFAULT_RSA_KEY_SIZE = "2048";

	/**
	 * The cert lifetime requested by istio agent.
	 */
	public final static String SECRET_TTL_KEY = "SECRET_TTL";

	/**
	 * The cert lifetime default value 24h0m0s.
	 */
	public final static String DEFAULT_SECRET_TTL = "86400"; // 24 * 60 * 60

	/**
	 * Default istio meta cluster id.
	 */
	public final static String DEFAULT_ISTIO_META_CLUSTER_ID = "Kubernetes";

	/**
	 * Istio CA path.
	 */
	public final static String ISTIO_CA_PATH = "/var/run/secrets/istio/root-cert.pem";

	/**
	 * Spiffe prefix.
	 */
	public final static String SPIFFE = "spiffe://";

	/**
	 * NS.
	 */
	public final static String NS = "/ns/";

	/**
	 * SA.
	 */
	public final static String SA = "/sa/";

	/**
	 * Address of the spiffe certificate provider. Defaults to discoveryAddress.
	 */
	public final static String CA_ADDR_KEY = "CA_ADDR";

	/**
	 * The trust domain for spiffe certificates.
	 */
	public final static String TRUST_DOMAIN_KEY = "TRUST_DOMAIN";

	/**
	 * The grace period ratio for the cert rotation.
	 */
	public final static String SECRET_GRACE_PERIOD_RATIO_KEY = "SECRET_GRACE_PERIOD_RATIO";

	/**
	 * The grace period ratio for the cert rotation, by default 0.5.
	 */
	public final static String DEFAULT_SECRET_GRACE_PERIOD_RATIO = "0.5";

	/**
	 * The type of ECC signature algorithm to use when generating private keys.
	 */
	public final static String ECC_SIG_ALG_KEY = "ECC_SIGNATURE_ALGORITHM";

	/**
	 * Default ecc algorithm.
	 */
	public final static String DEFAULT_ECC_SIG_ALG = "ECDSA";

	/**
	 * Service account key.
	 */
	public final static String SERVICE_ACCOUNT_KEY = "SERVICE_ACCOUNT";

	private IstioConstants() {

	}

}
