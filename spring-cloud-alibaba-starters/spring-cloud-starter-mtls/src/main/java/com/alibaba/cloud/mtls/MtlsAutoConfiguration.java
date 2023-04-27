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

import com.alibaba.cloud.governance.istio.XdsAutoConfiguration;
import com.alibaba.cloud.governance.istio.sds.AbstractCertManager;
import com.alibaba.cloud.mtls.client.rest.ClientRequestFactoryProvider;
import com.alibaba.cloud.mtls.server.tomcat.MtlsTomcatConnectCustomizer;
import org.apache.catalina.startup.Tomcat;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(XdsAutoConfiguration.class)
public class MtlsAutoConfiguration {

	@Bean
	public MtlsSslStoreProvider mtlsSslStoreProvider(AbstractCertManager certManager) {
		return new MtlsSslStoreProvider(certManager);
	}

	@Bean
	@ConditionalOnClass(Tomcat.class)
	public TomcatConnectorCustomizer mtlsCustomizer(MtlsSslStoreProvider sslStoreProvider,
			AbstractCertManager certManager,
			ClientRequestFactoryProvider clientRequestFactoryProvider) {
		return new MtlsTomcatConnectCustomizer(sslStoreProvider, certManager,
				clientRequestFactoryProvider);
	}

}
