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

package com.alibaba.cloud.mtls.client;

import java.security.KeyStore;

import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.governance.istio.sds.CertUpdateCallback;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import nl.altindag.ssl.SSLFactory;
import nl.altindag.ssl.util.SSLFactoryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCertUpdateCallback implements CertUpdateCallback {

	private static final Logger log = LoggerFactory
			.getLogger(ClientCertUpdateCallback.class);

	private MtlsSslStoreProvider mtlsSslStoreProvider;

	private SSLFactory originFactory;

	public ClientCertUpdateCallback(MtlsSslStoreProvider mtlsSslStoreProvider,
			SSLFactory originFactory) {
		this.mtlsSslStoreProvider = mtlsSslStoreProvider;
		this.originFactory = originFactory;
	}

	@Override
	public synchronized void onUpdateCert(CertPair certPair) {
		try {
			KeyStore keyStore = mtlsSslStoreProvider.getKeyStore();
			KeyStore trustStore = mtlsSslStoreProvider.getTrustStore();
			SSLFactory factory = SSLFactory.builder().withUnsafeHostnameVerifier()
					.withTrustMaterial(trustStore)
					.withIdentityMaterial(keyStore, "".toCharArray()).build();
			SSLFactoryUtils.reload(originFactory, factory);
		}
		catch (Throwable t) {
			log.error("Failed to refresh x509KeyManager", t);
		}
	}

}
