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

package com.alibaba.cloud.governance.opensergo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.opensergo.config.enabled",
		matchIfMissing = true)
@EnableConfigurationProperties(OpenSergoConfigProperties.class)
@AutoConfigureOrder(OpenSergoAutoConfig.OPENSERGO_RESOURCE_AUTO_CONFIG_ORDER)
public class OpenSergoAutoConfig {

	/**
	 * Order of OpenSergo auto config.
	 */
	public static final int OPENSERGO_RESOURCE_AUTO_CONFIG_ORDER = 101;

	@Autowired
	private OpenSergoConfigProperties openSergoConfigProperties;

	@Bean
	public OpenSergoTrafficRouterParser openSergoTrafficRouterParser() {
		return new OpenSergoTrafficRouterParser();
	}

	@Bean
	public OpenSergoTrafficExchanger openSergoTrafficExchanger(
			OpenSergoTrafficRouterParser openSergoTrafficRouterParser) {
		return new OpenSergoTrafficExchanger(openSergoConfigProperties,
				openSergoTrafficRouterParser);
	}

	@Bean
	public TargetServiceChangedListener targetServiceChangedListener(
			OpenSergoTrafficExchanger openSergoTrafficExchanger) {
		return new TargetServiceChangedListener(openSergoConfigProperties,
				openSergoTrafficExchanger);
	}

}
