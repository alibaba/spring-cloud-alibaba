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

package com.alibaba.cloud.governance.opensergo;

import com.alibaba.cloud.governance.opensergo.listener.TargetServiceChangedListener;
import com.alibaba.cloud.router.data.ControlPlaneAutoConfiguration;
import com.alibaba.cloud.router.data.controlplane.ControlPlaneConnection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.opensergo.config.enabled",
		matchIfMissing = true)
@AutoConfigureAfter({ ControlPlaneAutoConfiguration.class })
@EnableConfigurationProperties(OpenSergoConfigProperties.class)
public class OpenSergoAutoConfig {

	@Autowired
	private OpenSergoConfigProperties openSergoConfigProperties;

	@Bean
	public OpenSergoTrafficExchanger openSergoTrafficExchanger(
			ControlPlaneConnection controlPlaneConnection) {
		return new OpenSergoTrafficExchanger(openSergoConfigProperties,
				controlPlaneConnection);
	}

	@Bean
	public TargetServiceChangedListener targetServiceChangedListener(
			OpenSergoTrafficExchanger openSergoTrafficExchanger) {
		return new TargetServiceChangedListener(openSergoConfigProperties,
				openSergoTrafficExchanger);
	}

}
