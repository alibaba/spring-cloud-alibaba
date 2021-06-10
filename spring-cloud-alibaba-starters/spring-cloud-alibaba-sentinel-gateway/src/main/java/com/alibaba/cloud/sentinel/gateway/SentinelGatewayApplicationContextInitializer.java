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

package com.alibaba.cloud.sentinel.gateway;

import com.alibaba.csp.sentinel.config.SentinelConfig;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * @author <a href="mailto:349339884@qq.com">oawang</a>
 */
public class SentinelGatewayApplicationContextInitializer implements
		ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		initAppType();
	}

	private void initAppType() {
		System.setProperty(SentinelConfig.APP_TYPE_PROP_KEY,
				ConfigConstants.APP_TYPE_SCG_GATEWAY);
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
