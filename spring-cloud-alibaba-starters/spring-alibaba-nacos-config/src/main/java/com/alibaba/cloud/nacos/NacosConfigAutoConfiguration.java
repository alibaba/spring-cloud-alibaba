/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.nacos;

import com.alibaba.cloud.nacos.annotation.NacosAnnotationProcessor;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for Nacos configuration.
 * Registers core Nacos config beans (properties, manager, refresher, etc.) when Nacos config is enabled.
 *
 * @author juven.xuxb
 * @author freeman
 */
@Configuration(proxyBeanMethods = false)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigAutoConfiguration {


	/**
	 * Creates NacosConfigProperties bean with priority to parent context if exists.
	 * Falls back to NacosConfigManager's properties or new instance for unit test scenarios.
	 *
	 * @param context Spring ApplicationContext to retrieve parent context beans
	 * @return NacosConfigProperties instance
	 */
	@Bean
	@ConditionalOnMissingBean(value = NacosConfigProperties.class, search = SearchStrategy.CURRENT)
	public NacosConfigProperties nacosConfigProperties(ApplicationContext context) {
		if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(),
				NacosConfigProperties.class).length > 0) {
			return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), NacosConfigProperties.class);
		}
		// Fallback for unit tests where NacosConfigManager is not initialized
		if (NacosConfigManager.getInstance() == null) {
			return new NacosConfigProperties();
		}
		else {
			return NacosConfigManager.getInstance().getNacosConfigProperties();
		}
	}

	/**
	 * Creates NacosRefreshHistory bean to track Nacos config refresh history.
	 *
	 * @return NacosRefreshHistory instance
	 */
	@Bean
	public NacosRefreshHistory nacosRefreshHistory() {
		return new NacosRefreshHistory();
	}

	/**
	 * Creates NacosConfigManager bean initialized with NacosConfigProperties.
	 * Uses singleton pattern via NacosConfigManager.getInstance().
	 *
	 * @param nacosConfigProperties Nacos configuration properties
	 * @return NacosConfigManager singleton instance
	 */
	@Bean
	public NacosConfigManager nacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		return NacosConfigManager.getInstance(nacosConfigProperties);
	}

	/**
	 * Creates static NacosAnnotationProcessor bean to process Nacos annotations.
	 *
	 * @return NacosAnnotationProcessor instance
	 */
	@Bean
	public static NacosAnnotationProcessor nacosAnnotationProcessor() {
		return new NacosAnnotationProcessor();
	}

	/**
	 * Creates NacosContextRefresher bean to handle Nacos config refresh events.
	 * Uses new configuration logic (no compatibility with legacy config by default).
	 *
	 * @param nacosConfigManager Nacos config manager instance
	 * @param nacosRefreshHistory Nacos refresh history tracker
	 * @return NacosContextRefresher instance
	 */
	@Bean
	public NacosContextRefresher nacosContextRefresher(NacosConfigManager nacosConfigManager,
			NacosRefreshHistory nacosRefreshHistory) {
		// Legacy config compatibility is not required by default; use new config if needed
		return new NacosContextRefresher(nacosConfigManager, nacosRefreshHistory);
	}

}
