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
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509ExtendedTrustManager;

import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import nl.altindag.ssl.SSLFactory;

public class MtlsClientSSLContext {

	private SSLFactory sslFactory;

	public MtlsClientSSLContext(MtlsSslStoreProvider mtlsSslStoreProvider,
			AbstractCertManager abstractCertManager) {
		if (sslFactory == null) {
			synchronized (MtlsClientSSLContext.class) {
				if (sslFactory == null) {
					KeyStore keyStore = mtlsSslStoreProvider.getKeyStore();
					// init trust store
					KeyStore trustStore = mtlsSslStoreProvider.getTrustStore();
					sslFactory = SSLFactory.builder().withUnsafeHostnameVerifier()
							.withSwappableIdentityMaterial().withSwappableTrustMaterial()
							.withIdentityMaterial(keyStore, "".toCharArray())
							.withTrustMaterial(trustStore).build();
					abstractCertManager.registerCallback(new ClientCertUpdateCallback(
							mtlsSslStoreProvider, sslFactory));
				}
			}
		}
	}

	public SSLContext getSslContext() {
		return sslFactory.getSslContext();
	}

	public SSLSocketFactory getSslSocketFactory() {
		return sslFactory.getSslSocketFactory();
	}

	public SSLServerSocketFactory getSslServerSocketFactory() {
		return sslFactory.getSslServerSocketFactory();
	}

	public Optional<X509ExtendedKeyManager> getKeyManager() {
		return sslFactory.getKeyManager();
	}

	public Optional<KeyManagerFactory> getKeyManagerFactory() {
		return sslFactory.getKeyManagerFactory();
	}

	public Optional<X509ExtendedTrustManager> getTrustManager() {
		return sslFactory.getTrustManager();
	}

	public Optional<TrustManagerFactory> getTrustManagerFactory() {
		return sslFactory.getTrustManagerFactory();
	}

	public List<X509Certificate> getTrustedCertificates() {
		return sslFactory.getTrustedCertificates();
	}

	public HostnameVerifier getHostnameVerifier() {
		return sslFactory.getHostnameVerifier();
	}

	public List<String> getCiphers() {
		return sslFactory.getCiphers();
	}

	public List<String> getProtocols() {
		return sslFactory.getProtocols();
	}

	public SSLParameters getSslParameters() {
		return sslFactory.getSslParameters();
	}

	public SSLEngine getSSLEngine() {
		return sslFactory.getSSLEngine();
	}

	public SSLEngine getSSLEngine(String peerHost, Integer peerPort) {
		return sslFactory.getSSLEngine(peerHost, peerPort);
	}

}
