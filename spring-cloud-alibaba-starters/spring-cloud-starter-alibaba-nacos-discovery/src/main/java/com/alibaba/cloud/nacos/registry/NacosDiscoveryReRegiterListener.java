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

package com.alibaba.cloud.nacos.registry;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.event.NacosDiscoveryReRegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class NacosDiscoveryReRegiterListener {

	private static final Logger log = LoggerFactory
			.getLogger(NacosDiscoveryReRegiterListener.class);

	@Autowired
	private NacosServiceManager nacosServiceManager;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	private NacosAutoServiceRegistration nacosAutoServiceRegistration;

	@Order
	@EventListener
	public void listener(RefreshEvent refreshedEvent) {
		NacosDiscoveryProperties properties = nacosAutoServiceRegistration
				.getRegistration().getNacosDiscoveryProperties();
		if (nacosServiceManager.isAnyChanged(properties)) {
			if (nacosServiceManager.isCoreChanged(properties)) {
				log.info("nacos discovery deep changed, will restart");
				applicationEventPublisher.publishEvent(
						new NacosDiscoveryReRegisterEvent(properties, true, false));
			}
			else {
				log.info("nacos discovery shallow changed, will upgrade");
				applicationEventPublisher.publishEvent(
						new NacosDiscoveryReRegisterEvent(properties, false, true));
			}
		}
		else {
			log.info("nacos discovery changed, with do nothing");
			// do nothing
		}
	}

}
