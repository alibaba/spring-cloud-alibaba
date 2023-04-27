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

package com.alibaba.cloud.mtls;

import java.security.KeyStore;

import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.mtls.constants.MtlsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.server.SslStoreProvider;

public class MtlsSslStoreProvider implements SslStoreProvider {

	private static final Logger log = LoggerFactory.getLogger(MtlsSslStoreProvider.class);

	private final AbstractCertManager certManager;

	public MtlsSslStoreProvider(AbstractCertManager certManager) {
		this.certManager = certManager;
	}

	@Override
	public KeyStore getKeyStore() {
		CertPair certPair = certManager.getCertPair();
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setKeyEntry(MtlsConstants.MTLS_DEFAULT_KEY_STORE_ALIAS,
					certPair.getPrivateKey(), "".toCharArray(),
					certPair.getCertificateChain());
			return keyStore;
		}
		catch (Exception e) {
			log.error("Unable to get key store", e);
		}
		return null;
	}

	@Override
	public KeyStore getTrustStore() {
		CertPair certPair = certManager.getCertPair();
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setCertificateEntry(MtlsConstants.MTLS_DEFAULT_TRUST_STORE_ALIAS,
					certPair.getCertificateChain()[certPair.getCertificateChain().length
							- 1]);
			return keyStore;
		}
		catch (Exception e) {
			log.error("Unable to get trust store", e);
		}
		return null;
	}

}
