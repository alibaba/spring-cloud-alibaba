/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.security.trust;

import java.security.KeyStore;

import com.alibaba.csp.sentinel.trust.TrustManager;
import com.alibaba.csp.sentinel.trust.cert.CertPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.web.server.SslStoreProvider;


public class TrustSslStoreProvider implements SslStoreProvider {

	private static final Logger log = LoggerFactory.getLogger(TrustSslStoreProvider.class);

	/**
	 *  key store alias key.
	 */
	public static final String MTLS_DEFAULT_KEY_STORE_ALIAS = "mtls-default-key-store";

	/**
	 * trust store alias key.
	 */
	public static final String MTLS_DEFAULT_TRUST_STORE_ALIAS = "mtls-default-trust-store";


	private TrustManager trustManager = TrustManager.getInstance();

	public TrustSslStoreProvider() {
	}


	@Override
	public KeyStore getKeyStore() {
		CertPair certPair = trustManager.getCertPair();

		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setKeyEntry(MTLS_DEFAULT_KEY_STORE_ALIAS,
					certPair.getPrivateKey(), "".toCharArray(),
					certPair.getCertificateChain());
			return keyStore;
		}
		catch (Exception e) {
			log.error("Error in getting key store", e);
			return null;
		}
	}

	@Override
	public KeyStore getTrustStore() {

		CertPair certPair = trustManager.getCertPair();

		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(null, null);
			keyStore.setCertificateEntry(MTLS_DEFAULT_TRUST_STORE_ALIAS,
					certPair.getRootCA());
			return keyStore;
		}
		catch (Exception e) {
			log.error("Error in getting trust store", e);
			return null;
		}
	}
}

