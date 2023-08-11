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

package com.alibaba.cloud.mtls.server.netty;

import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManagerFactorySpi;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslConfigurationValidator;
import org.springframework.boot.web.server.WebServerException;

public class MtlsNettyServerCustomizer implements NettyServerCustomizer {

	private static final Logger log = LoggerFactory
			.getLogger(MtlsNettyServerCustomizer.class);

	private final AbstractCertManager certManager;

	private final MtlsSslStoreProvider sslStoreProvider;

	public MtlsNettyServerCustomizer(AbstractCertManager certManager,
			MtlsSslStoreProvider sslStoreProvider) {
		this.certManager = certManager;
		this.sslStoreProvider = sslStoreProvider;
	}

	@Override
	public HttpServer apply(HttpServer httpServer) {
		// todo:validateContext()

		// todo：证书过期，回调

		try {
			return httpServer.secure((contextSpec) -> {
				SslProvider.DefaultConfigurationSpec spec = contextSpec
						.sslContext(getContextBuilder());
			});
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private SslContextBuilder getContextBuilder() {
		Ssl ssl = new Ssl();
		ssl.setClientAuth(Ssl.ClientAuth.WANT); // 请求客户端认证但不强制
		SslContextBuilder builder = SslContextBuilder
				.forServer(getKeyManagerFactory(ssl, this.sslStoreProvider))
				.trustManager(getTrustManagerFactory(this.sslStoreProvider));
		if (ssl.getEnabledProtocols() != null) {
			builder.protocols(ssl.getEnabledProtocols());
		}
		if (ssl.getCiphers() != null) {
			builder.ciphers(Arrays.asList(ssl.getCiphers()));
		}
		builder.clientAuth(ClientAuth.OPTIONAL); // 客户端认证模式为OPTIONAL。这意味着服务器会请求客户端证书，但即使客户端不提供证书，SSL/TLS握手仍将成功。

		return builder;
	}

	private KeyManagerFactory getKeyManagerFactory(Ssl ssl,
			MtlsSslStoreProvider sslStoreProvider) {
		try {
			KeyStore keyStore = null;
			try {
				if (sslStoreProvider != null) {
					keyStore = sslStoreProvider.getKeyStore();
				}
			}
			catch (Exception ex) {
				throw new WebServerException("Could not load store: " + ex.getMessage(),
						ex);
			}
			SslConfigurationValidator.validateKeyAlias(keyStore, ssl.getKeyAlias());

			KeyManagerFactory keyManagerFactory = null;
			if (ssl.getKeyAlias() == null) {
				keyManagerFactory = KeyManagerFactory
						.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			}
			else {
				keyManagerFactory = new ConfigurableAliasKeyManagerFactory(
						ssl.getKeyAlias(), KeyManagerFactory.getDefaultAlgorithm());
			}
			/*
			 * char[] keyPassword = (ssl.getKeyPassword() != null) ?
			 * ssl.getKeyPassword().toCharArray() : null; if (keyPassword == null &&
			 * ssl.getKeyStorePassword() != null) { keyPassword =
			 * ssl.getKeyStorePassword().toCharArray(); }
			 */
			String keyPass = "";
			keyManagerFactory.init(keyStore, keyPass.toCharArray());
			return keyManagerFactory;
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private TrustManagerFactory getTrustManagerFactory(
			MtlsSslStoreProvider sslStoreProvider) {
		try {
			KeyStore store = null;
			try {
				if (sslStoreProvider != null) {
					store = sslStoreProvider.getTrustStore();
				}
			}
			catch (Exception ex) {
				throw new WebServerException("Could not load store: " + ex.getMessage(),
						ex);
			}
			TrustManagerFactory trustManagerFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(store);
			return trustManagerFactory;
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
	}

	private static final class ConfigurableAliasKeyManagerFactory
			extends KeyManagerFactory {

		private ConfigurableAliasKeyManagerFactory(String alias, String algorithm)
				throws NoSuchAlgorithmException {
			this(KeyManagerFactory.getInstance(algorithm), alias, algorithm);
		}

		private ConfigurableAliasKeyManagerFactory(KeyManagerFactory delegate,
				String alias, String algorithm) {
			super(new ConfigurableAliasKeyManagerFactorySpi(delegate, alias),
					delegate.getProvider(), algorithm);
		}

	}

	private static final class ConfigurableAliasKeyManagerFactorySpi
			extends KeyManagerFactorySpi {

		private final KeyManagerFactory delegate;

		private final String alias;

		private ConfigurableAliasKeyManagerFactorySpi(KeyManagerFactory delegate,
				String alias) {
			this.delegate = delegate;
			this.alias = alias;
		}

		@Override
		protected void engineInit(KeyStore keyStore, char[] chars)
				throws KeyStoreException, NoSuchAlgorithmException,
				UnrecoverableKeyException {
			this.delegate.init(keyStore, chars);
		}

		@Override
		protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
				throws InvalidAlgorithmParameterException {
			throw new InvalidAlgorithmParameterException(
					"Unsupported ManagerFactoryParameters");
		}

		@Override
		protected KeyManager[] engineGetKeyManagers() {
			return Arrays.stream(this.delegate.getKeyManagers())
					.filter(X509ExtendedKeyManager.class::isInstance)
					.map(X509ExtendedKeyManager.class::cast).map(this::wrap)
					.toArray(KeyManager[]::new);
		}

		private ConfigurableAliasKeyManager wrap(X509ExtendedKeyManager keyManager) {
			return new ConfigurableAliasKeyManager(keyManager, this.alias);
		}

	}

	private static final class ConfigurableAliasKeyManager
			extends X509ExtendedKeyManager {

		private final X509ExtendedKeyManager delegate;

		private final String alias;

		private ConfigurableAliasKeyManager(X509ExtendedKeyManager keyManager,
				String alias) {
			this.delegate = keyManager;
			this.alias = alias;
		}

		@Override
		public String chooseEngineClientAlias(String[] strings, Principal[] principals,
				SSLEngine sslEngine) {
			return this.delegate.chooseEngineClientAlias(strings, principals, sslEngine);
		}

		@Override
		public String chooseEngineServerAlias(String s, Principal[] principals,
				SSLEngine sslEngine) {
			return (this.alias != null) ? this.alias
					: this.delegate.chooseEngineServerAlias(s, principals, sslEngine);
		}

		@Override
		public String chooseClientAlias(String[] keyType, Principal[] issuers,
				Socket socket) {
			return this.delegate.chooseClientAlias(keyType, issuers, socket);
		}

		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers,
				Socket socket) {
			return this.delegate.chooseServerAlias(keyType, issuers, socket);
		}

		@Override
		public X509Certificate[] getCertificateChain(String alias) {
			return this.delegate.getCertificateChain(alias);
		}

		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers) {
			return this.delegate.getClientAliases(keyType, issuers);
		}

		@Override
		public PrivateKey getPrivateKey(String alias) {
			return this.delegate.getPrivateKey(alias);
		}

		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers) {
			return this.delegate.getServerAliases(keyType, issuers);
		}

	}

}
