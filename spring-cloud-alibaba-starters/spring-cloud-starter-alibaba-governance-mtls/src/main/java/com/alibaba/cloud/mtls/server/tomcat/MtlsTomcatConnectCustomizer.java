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

package com.alibaba.cloud.mtls.server.tomcat;

import com.alibaba.cloud.commons.governance.tls.ServerTlsModeHolder;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.governance.istio.sds.CertPair;
import com.alibaba.cloud.governance.istio.sds.CertUpdateCallback;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11JsseProtocol;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

public class MtlsTomcatConnectCustomizer
		implements TomcatConnectorCustomizer, ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(MtlsTomcatConnectCustomizer.class);

	private final AbstractCertManager certManager;

	private final MtlsSslStoreProvider sslStoreProvider;

	private ApplicationContext applicationContext;

	public MtlsTomcatConnectCustomizer(MtlsSslStoreProvider sslStoreProvider,
			AbstractCertManager certManager) {
		this.certManager = certManager;
		this.sslStoreProvider = sslStoreProvider;
	}

	@Override
	public void customize(Connector connector) {
		if (!validateContext()) {
			return;
		}
		// When the certificate is expired, we refresh the server certificate.
		certManager.registerCallback(new CertUpdateCallback() {
			@Override
			public synchronized void onUpdateCert(CertPair certPair) {
				try {
					if (!validateContext()) {
						return;
					}
					ProtocolHandler protocolHandler = connector.getProtocolHandler();
					AbstractProtocol<?> abstractProtocol = (AbstractProtocol<?>) protocolHandler;
					if (abstractProtocol instanceof AbstractHttp11Protocol<?>) {
						AbstractHttp11Protocol<?> proto = ((AbstractHttp11Protocol<?>) abstractProtocol);
						proto.reloadSslHostConfigs();
					}
				}
				catch (Exception e) {
					log.error("Failed to reload certificate of tomcat", e);
				}
			}
		});
		if (!ServerTlsModeHolder.getTlsMode()) {
			log.warn("Fetch tls mode failed, use plaintext to transport");
			return;
		}
		if (!ServerTlsModeHolder.getTlsMode()) {
			return;
		}
		try {
			ProtocolHandler handler = connector.getProtocolHandler();
			AbstractHttp11JsseProtocol<?> protocol = (AbstractHttp11JsseProtocol<?>) handler;
			Ssl ssl = new Ssl();
			ssl.setClientAuth(Ssl.ClientAuth.WANT);
			protocol.setSSLEnabled(true);
			protocol.setSslProtocol(ssl.getProtocol());
			configureSslClientAuth(protocol, ssl);
			protocol.setKeyAlias(ssl.getKeyAlias());
			String ciphers = StringUtils.arrayToCommaDelimitedString(ssl.getCiphers());
			if (StringUtils.hasText(ciphers)) {
				protocol.setCiphers(ciphers);
			}
			TomcatURLStreamHandlerFactory tomcatURLStreamHandlerFactory = TomcatURLStreamHandlerFactory
					.getInstance();
			SslStoreProviderUrlStreamHandlerFactory sslStoreProviderUrlStreamHandlerFactory = new SslStoreProviderUrlStreamHandlerFactory(
					sslStoreProvider);
			TomcatURLStreamHandlerFactory.release(
					sslStoreProviderUrlStreamHandlerFactory.getClass().getClassLoader());
			tomcatURLStreamHandlerFactory
					.addUserFactory(sslStoreProviderUrlStreamHandlerFactory);
			try {
				if (sslStoreProvider.getKeyStore() != null) {
					protocol.setKeystorePass("");
					protocol.setKeystoreFile(
							SslStoreProviderUrlStreamHandlerFactory.KEY_STORE_URL);
				}
				if (sslStoreProvider.getTrustStore() != null) {
					protocol.setTruststorePass("");
					protocol.setTruststoreFile(
							SslStoreProviderUrlStreamHandlerFactory.TRUST_STORE_URL);
				}
			}
			catch (Exception ex) {
				throw new WebServerException("Could not load store: " + ex.getMessage(),
						ex);
			}
			connector.setScheme("https");
			connector.setSecure(true);
		}
		catch (Exception e) {
			log.error("Custom tomcat ssl failed!", e);
		}
	}

	private boolean validateContext() {
		if (applicationContext instanceof ConfigurableApplicationContext) {
			ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
			return context.isActive();
		}
		return true;
	}

	private void configureSslClientAuth(AbstractHttp11JsseProtocol<?> protocol, Ssl ssl) {
		if (ssl.getClientAuth() == Ssl.ClientAuth.NEED) {
			protocol.setClientAuth(Boolean.TRUE.toString());
		}
		else if (ssl.getClientAuth() == Ssl.ClientAuth.WANT) {
			protocol.setClientAuth("want");
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
