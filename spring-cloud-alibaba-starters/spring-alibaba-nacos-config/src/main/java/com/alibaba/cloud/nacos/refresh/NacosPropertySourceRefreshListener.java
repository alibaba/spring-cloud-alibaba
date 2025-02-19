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

package com.alibaba.cloud.nacos.refresh;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.client.NacosPropertySourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

public class NacosPropertySourceRefreshListener implements BeanPostProcessor, SmartApplicationListener, ApplicationContextAware {

	private final static Logger log = LoggerFactory
			.getLogger(NacosPropertySourceRefreshListener.class);

	private Map<String, ConfigurationPropertiesBean> beans = new HashMap<>();

	private ApplicationContext applicationContext;

	private AtomicBoolean ready = new AtomicBoolean(false);

	NacosConfigManager nacosConfigManager;

	public NacosPropertySourceRefreshListener(NacosConfigManager nacosConfigManager) {
		this.nacosConfigManager = nacosConfigManager;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

		ConfigurationPropertiesBean propertiesBean = ConfigurationPropertiesBean.get(this.applicationContext, bean,
				beanName);
		if (propertiesBean != null) {
			this.beans.put(beanName, propertiesBean);
		}
		return bean;
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return ApplicationReadyEvent.class.isAssignableFrom(eventType) || NacosConfigRefreshEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void handle(ApplicationReadyEvent event) {
		this.ready.compareAndSet(false, true);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {

		if (event instanceof ApplicationReadyEvent) {
			handle((ApplicationReadyEvent) event);
		}
		else if (event instanceof NacosConfigRefreshEvent) {
			handle((NacosConfigRefreshEvent) event);
		}
	}

	public void handle(NacosConfigRefreshEvent event) {
		if (this.ready.get()) { // don't handle events before app is ready
			if (!applicationContext.containsBean("nacosConfigSpringCloudRefreshEventListener")) {
				log.info("Event received " + event.getEventDesc());

				NacosPropertySourceBuilder nacosPropertySourceBuilder = new NacosPropertySourceBuilder(nacosConfigManager.getConfigService(), nacosConfigManager.getNacosConfigProperties()
						.getTimeout());
				String sourceName = String.join(NacosConfigProperties.COMMAS, event.dataId, event.group);
				ConfigurableEnvironment environment = ((ConfigurableApplicationContext) applicationContext).getEnvironment();
				MutablePropertySources target = environment.getPropertySources();
				PropertySource<?> prevpropertySource = target.get(sourceName);
				if (prevpropertySource instanceof NacosPropertySource) {
					NacosPropertySource newProperSource = nacosPropertySourceBuilder.build(event.getDataId(), event.getGroup(), "properties", ((NacosPropertySource) prevpropertySource).isRefreshable());
					target.replace(sourceName, newProperSource);
					log.info("Replace Nacos Property Source : " + sourceName);

				}

			}

		}
	}
}
