/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.governance.istio;

import com.alibaba.cloud.governance.auth.AuthDataAutoConfiguration;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.istio.protocol.impl.CdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.EdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.RdsProtocol;
import com.alibaba.cloud.router.data.ControlPlaneAutoConfiguration;
import com.alibaba.cloud.router.data.controlplane.ControlPlaneConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter({ ControlPlaneAutoConfiguration.class,
		AuthDataAutoConfiguration.class })
@ConditionalOnProperty(name = "spring.cloud.istio.config.enabled", matchIfMissing = true)
@EnableConfigurationProperties(XdsConfigProperties.class)
public class XdsAutoConfiguration {

	@Autowired
	private XdsConfigProperties xdsConfigProperties;

	@Bean
	public XdsChannel xdsChannel() {
		return new XdsChannel(xdsConfigProperties);
	}

	@Bean
	public XdsScheduledThreadPool xdsScheduledThreadPool() {
		return new XdsScheduledThreadPool(xdsConfigProperties);
	}

	@Bean
	public PilotExchanger pilotExchanger(LdsProtocol ldsProtocol, CdsProtocol cdsProtocol,
			EdsProtocol edsProtocol, RdsProtocol rdsProtocol) {
		return new PilotExchanger(ldsProtocol, cdsProtocol, edsProtocol, rdsProtocol);
	}

	@Bean
	public LdsProtocol ldsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			AuthRepository authRepository) {
		return new LdsProtocol(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties,
				authRepository);
	}

	@Bean
	public CdsProtocol cdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new CdsProtocol(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties);
	}

	@Bean
	EdsProtocol edsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool) {
		return new EdsProtocol(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties);
	}

	@Bean
	RdsProtocol rdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool,
			ControlPlaneConnection controlPlaneConnection) {
		return new RdsProtocol(xdsChannel, xdsScheduledThreadPool, xdsConfigProperties,
				controlPlaneConnection);
	}

}
