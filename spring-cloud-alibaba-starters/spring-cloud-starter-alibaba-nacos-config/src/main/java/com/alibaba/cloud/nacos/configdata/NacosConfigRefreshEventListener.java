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

package com.alibaba.cloud.nacos.configdata;

import com.alibaba.cloud.nacos.refresh.NacosConfigRefreshEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 *
 * @author shiyiyue
 * @since 2024.10.17
 */
public class NacosConfigRefreshEventListener implements SmartApplicationListener, ApplicationContextAware {

	private final static Logger log = LoggerFactory.getLogger(NacosConfigRefreshEventListener.class);

	private ApplicationContext applicationContext;

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return NacosConfigRefreshEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		applicationContext.publishEvent(new RefreshEvent(event.getSource(), null, "Refresh Nacos config"));
		if (log.isDebugEnabled()) {
			log.debug(String.format("Refresh Nacos config group=%s,dataId=%s", ((NacosConfigRefreshEvent) event).getGroup(), ((NacosConfigRefreshEvent) event).getDataId()));
		}
	}
}
