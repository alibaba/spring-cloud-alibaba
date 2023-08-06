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

import java.util.List;

import com.alibaba.cloud.commons.governance.ControlPlaneInitedBean;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.governance.istio.sds.CertUpdateCallback;
import com.alibaba.cloud.mtls.client.rest.ClientRequestFactoryProvider;
import com.alibaba.cloud.mtls.client.rest.RestTemplateCallback;
import com.alibaba.cloud.mtls.server.ApplicationRestarter;
import com.alibaba.cloud.mtls.server.ServerTlsModeHolder;
import com.alibaba.cloud.mtls.server.ServerTlsModeListener;
import com.alibaba.cloud.mtls.server.tomcat.MtlsTomcatConnectCustomizer;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistrationCustomizer;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
public class MtlsAutoConfiguration {

	private static final Logger log = LoggerFactory
			.getLogger(MtlsAutoConfiguration.class);

	@Bean
	public MtlsSslStoreProvider mtlsSslStoreProvider(AbstractCertManager certManager) {
		return new MtlsSslStoreProvider(certManager);
	}

	@Bean
	public ApplicationRestarter applicationRestarter() {
		return new ApplicationRestarter();
	}

	@Bean
	public ServerTlsModeListener serverTlsModeListener(
			ApplicationRestarter applicationRestarter) {
		return new ServerTlsModeListener(applicationRestarter);
	}

	@Bean
	public MtlsCertCallbackIniter mtlsClientCallbackIniter(
			AbstractCertManager certManager,
			@Autowired(required = false) List<CertUpdateCallback> callbacks) {
		return new MtlsCertCallbackIniter(certManager, callbacks);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ Tomcat.class })
	static class TomcatConnectionCustomizerConfiguration {

		@Bean
		public TomcatConnectorCustomizer mtlsCustomizer(
				MtlsSslStoreProvider sslStoreProvider, AbstractCertManager certManager,
				ServerTlsModeListener serverTlsModeListener,
				ControlPlaneInitedBean controlPlaneInitedBean) {
			ServerTlsModeHolder.setTlsMode(controlPlaneInitedBean.isTls());
			return new MtlsTomcatConnectCustomizer(sslStoreProvider, certManager);
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ NacosRegistrationCustomizer.class, NacosRegistration.class })
	static class NacosCustomizerConfiguration {

		@Bean
		public NacosRegistrationCustomizer nacosTlsCustomizer() {
			return registration -> {
				if (!ServerTlsModeHolder.waitTlsModeInitialized()) {
					log.warn("Fetch tls mode failed, use plaintext to transport");
					return;
				}
				registration.getNacosDiscoveryProperties()
						.setSecure(ServerTlsModeHolder.getTlsMode());
				registration.getNacosDiscoveryProperties().getMetadata().put("secure",
						"true");
			};
		}

	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ RestTemplate.class })
	static class RestTemplateMtlsConfiguration {

		@Bean
		public RestTemplateCallback restTemplateCallback(
				ClientRequestFactoryProvider clientRequestFactoryProvider) {
			return new RestTemplateCallback(clientRequestFactoryProvider);
		}

	}

}
