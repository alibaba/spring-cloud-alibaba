/*
 * Copyright 2013-2023 the original author or authors.
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
 * @author juven.xuxb
 * @author freeman
 */
@Configuration(proxyBeanMethods = false)
@Conditional(NacosConfigEnabledCondition.class)
public class NacosConfigAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(value = NacosConfigProperties.class, search = SearchStrategy.CURRENT)
	public NacosConfigProperties nacosConfigProperties(ApplicationContext context) {
		if (context.getParent() != null && BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context.getParent(),
				NacosConfigProperties.class).length > 0) {
			return BeanFactoryUtils.beanOfTypeIncludingAncestors(context.getParent(), NacosConfigProperties.class);
		}
		if (NacosConfigManager.getInstance() == null) { // this should never happen except for some unit tests
			return new NacosConfigProperties();
		}
		else {
			return NacosConfigManager.getInstance().getNacosConfigProperties();
		}
	}

	@Bean
	public NacosRefreshHistory nacosRefreshHistory() {
		return new NacosRefreshHistory();
	}

	@Bean
	public NacosConfigManager nacosConfigManager(NacosConfigProperties nacosConfigProperties) {
		return NacosConfigManager.getInstance(nacosConfigProperties);
	}

	@Bean
	public NacosAnnotationProcessor nacosAnnotationProcessor() {
		return new NacosAnnotationProcessor();
	}

	@Bean
	public NacosContextRefresher nacosContextRefresher(NacosConfigManager nacosConfigManager,
			NacosRefreshHistory nacosRefreshHistory) {
		// Consider that it is not necessary to be compatible with the previous
		// configuration
		// and use the new configuration if necessary.
		return new NacosContextRefresher(nacosConfigManager, nacosRefreshHistory);
	}

}
