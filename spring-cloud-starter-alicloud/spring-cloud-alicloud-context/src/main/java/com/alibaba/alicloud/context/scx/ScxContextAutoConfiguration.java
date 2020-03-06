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

package com.alibaba.alicloud.context.scx;

import com.alibaba.alicloud.context.AliCloudProperties;
import com.alibaba.alicloud.context.edas.EdasContextAutoConfiguration;
import com.alibaba.alicloud.context.edas.EdasProperties;
import com.alibaba.cloud.context.edas.AliCloudEdasSdk;
import com.alibaba.cloud.context.scx.AliCloudScxInitializer;
import com.alibaba.edas.schedulerx.SchedulerXClient;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiaolongzuo
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.alibaba.alicloud.scx.ScxAutoConfiguration")
@ConditionalOnProperty(name = "spring.cloud.alicloud.scx.enabled", matchIfMissing = true)
@EnableConfigurationProperties(ScxProperties.class)
@ImportAutoConfiguration(EdasContextAutoConfiguration.class)
public class ScxContextAutoConfiguration {

	@Bean(initMethod = "init")
	@ConditionalOnMissingBean
	public SchedulerXClient schedulerXClient(AliCloudProperties aliCloudProperties,
			EdasProperties edasProperties, ScxProperties scxProperties,
			AliCloudEdasSdk aliCloudEdasSdk) {
		return AliCloudScxInitializer.initialize(aliCloudProperties, edasProperties,
				scxProperties, aliCloudEdasSdk);
	}

}
