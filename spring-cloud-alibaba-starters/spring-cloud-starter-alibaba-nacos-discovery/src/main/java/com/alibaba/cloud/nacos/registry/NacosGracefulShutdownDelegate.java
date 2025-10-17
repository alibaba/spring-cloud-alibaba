/*
 * Copyright 2013-2025 the original author or authors.
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
import com.alibaba.nacos.common.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.lang.NonNull;

/**
 * @author <a href="mailto:uuuyuqi@gmail.com">uuuyuqi</a>
 */
public class NacosGracefulShutdownDelegate implements ApplicationListener<ContextClosedEvent>, ApplicationContextAware {

	private static final Logger log = LoggerFactory.getLogger(NacosGracefulShutdownDelegate.class);

	private final NacosAutoServiceRegistration autoServiceRegistration;

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private ApplicationContext applicationContext;

	public NacosGracefulShutdownDelegate(NacosAutoServiceRegistration autoServiceRegistration,
		NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.autoServiceRegistration = autoServiceRegistration;
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void onApplicationEvent(@NonNull ContextClosedEvent event) {
		// should NOT be executed if ContextClosedEvent published by sub contexts
		if (!applicationContext.equals(event.getApplicationContext())) {
			log.debug("Nacos client graceful shutdown will NOT be executed "
					+ "for Spring context source: {}", event.getApplicationContext());
			return;
		}

		doGracefulShutdown();
	}

	protected void doGracefulShutdown() {
		try {
			autoServiceRegistration.stop();
			Integer gracefulShutdownWaitTime = this.nacosDiscoveryProperties.getGracefulShutdownWaitTime();
			if (gracefulShutdownWaitTime != null && gracefulShutdownWaitTime > 0) {
				ThreadUtils.sleep(gracefulShutdownWaitTime);
			}

			log.info("Nacos client graceful shutdown has been executed successfully. " +
					"Graceful shutdown wait time is {}", gracefulShutdownWaitTime);
		}
		catch (Throwable t) {
			log.error("Error occurred while performing Nacos client graceful shutdown", t);
		}
	}

	@Override
	public boolean supportsAsyncExecution() {
		// need wait for graceful shutdown
		return false;
	}
}
