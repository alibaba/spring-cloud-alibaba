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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.mtls.MtlsSslStoreProvider;
import com.alibaba.cloud.mtls.client.rest.ClientRequestFactoryProvider;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.client.RestTemplate;

public class MtlsTomcatConnectCustomizer
		implements TomcatConnectorCustomizer, ApplicationContextAware {

	private static final Logger log = LoggerFactory
			.getLogger(MtlsTomcatConnectCustomizer.class);

	private final MtlsSslStoreProvider sslStoreProvider;

	private final AbstractCertManager certManager;

	private final ClientRequestFactoryProvider clientRequestFactoryProvider;

	private ApplicationContext applicationContext;

	public MtlsTomcatConnectCustomizer(MtlsSslStoreProvider sslStoreProvider,
			AbstractCertManager certManager,
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		this.sslStoreProvider = sslStoreProvider;
		this.certManager = certManager;
		this.clientRequestFactoryProvider = clientRequestFactoryProvider;
	}

	@Override
	public void customize(Connector connector) {
		// When the certificate is expired, we refresh the server certificate.
		certManager.registerCallback(certPair -> {
			try {
				ProtocolHandler protocolHandler = connector.getProtocolHandler();
				AbstractProtocol<?> abstractProtocol = (AbstractProtocol<?>) protocolHandler;
				Method method = AbstractProtocol.class.getDeclaredMethod("getEndpoint");
				method.setAccessible(true);
				AbstractEndpoint<?, ?> endpoint = (AbstractEndpoint<?, ?>) method
						.invoke(abstractProtocol);
				Field configField = AbstractEndpoint.class
						.getDeclaredField("sslHostConfigs");
				configField.setAccessible(true);
				ConcurrentMap<String, SSLHostConfig> config = (ConcurrentMap<String, SSLHostConfig>) configField
						.get(endpoint);
				for (SSLHostConfig hostConfig : config.values()) {
					Set<SSLHostConfigCertificate> certificates = hostConfig
							.getCertificates();
					for (SSLHostConfigCertificate certificate : certificates) {
						certificate
								.setCertificateKeystore(sslStoreProvider.getKeyStore());
					}
				}
				endpoint.reloadSslHostConfigs();
			}
			catch (Exception e) {
				log.error("Failed to reload certificate of tomcat", e);
			}
		});
		// When the certificate is expired, we refresh the client certificate.
		certManager.registerCallback(certPair -> {
			try {
				Map<String, RestTemplate> restTemplates = applicationContext
						.getBeansOfType(RestTemplate.class);
				for (RestTemplate restTemplate : restTemplates.values()) {
					restTemplate.setRequestFactory(clientRequestFactoryProvider
							.getFactoryByTemplate(restTemplate));
				}
			}
			catch (BeanCreationException e1) {
				log.warn(
						"Spring is creating the RestTemplate bean, please try to refresh the client certificate later");
			}
			catch (Exception e2) {
				log.error("Failed to refresh RestTemplate", e2);
			}
		});
		try {
			Class<?> sslConnectorCustomizer = Class.forName(
					"org.springframework.boot.web.embedded.tomcat.SslConnectorCustomizer");
			Constructor<?> constructor = sslConnectorCustomizer
					.getDeclaredConstructor(Ssl.class, SslStoreProvider.class);
			constructor.setAccessible(true);
			Ssl ssl = new Ssl();
			ssl.setClientAuth(Ssl.ClientAuth.WANT);
			Object sslCustomizerInstance = constructor.newInstance(ssl, sslStoreProvider);
			Method custom = sslCustomizerInstance.getClass()
					.getDeclaredMethod("customize", Connector.class);
			custom.setAccessible(true);

			custom.invoke(sslCustomizerInstance, connector);

		}
		catch (Exception e) {
			log.error("Custom tomcat ssl failed!", e);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
