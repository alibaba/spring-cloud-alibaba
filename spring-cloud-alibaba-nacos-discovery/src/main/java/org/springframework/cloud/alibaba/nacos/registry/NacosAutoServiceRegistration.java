/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos.registry;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;

/**
 * @author xiaojing
 */
public class NacosAutoServiceRegistration
		implements AutoServiceRegistration, SmartLifecycle, Ordered {
	private static final Logger logger = LoggerFactory.getLogger(NacosAutoServiceRegistration.class);

	@Autowired
	private NacosRegistration registration;

	private int order = 0;
	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicInteger port = new AtomicInteger(0);
	private ApplicationContext context;
	private ServiceRegistry serviceRegistry;

	public NacosAutoServiceRegistration(ApplicationContext context,
										ServiceRegistry<NacosRegistration> serviceRegistry,
										NacosRegistration registration) {
		this.context = context;
		this.serviceRegistry = serviceRegistry;
		this.registration = registration;
	}

	@Override
	public void start() {
		if (this.port.get() != 0) {
			this.registration.setPort(port.get());
		}

		if (!this.running.get() && this.registration.getPort() > 0) {
			this.serviceRegistry.register(this.registration);
			this.context
					.publishEvent(new InstanceRegisteredEvent(this, this.registration));
			this.running.set(true);
		}
	}

	@Override
	public void stop() {
		this.serviceRegistry.deregister(this.registration);
		this.running.set(false);
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		this.stop();
		callback.run();
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	@EventListener(WebServerInitializedEvent.class)
	public void onApplicationEvent(WebServerInitializedEvent event) {
		int localPort = event.getWebServer().getPort();
		ApplicationContext context = event.getApplicationContext();

		if(!(context instanceof ConfigurableWebServerApplicationContext) || !"management".equals(((ConfigurableWebServerApplicationContext)context).getServerNamespace())) {
			logger.info("Updating port to {}", localPort);
			this.port.compareAndSet(0, event.getWebServer().getPort());
			this.start();
		}
	}

	@EventListener({ ContextClosedEvent.class })
	public void onApplicationEvent(ContextClosedEvent event) {
		if (event.getApplicationContext() == this.context) {
			this.stop();
		}

	}

}
