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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

/**
 * @author xiaojing
 */
public class NacosAutoServiceRegistration
		extends AbstractAutoServiceRegistration<NacosRegistration> {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(NacosAutoServiceRegistration.class);

	@Autowired
	private NacosRegistration registration;

	@Autowired
	private ApplicationContext context;

	public NacosAutoServiceRegistration(
			ServiceRegistry<NacosRegistration> serviceRegistry,
			AutoServiceRegistrationProperties properties,
			NacosRegistration registration) {
		super(serviceRegistry, properties);
		this.registration = registration;
	}

	@Override
	protected NacosRegistration getRegistration() {
		return registration;
	}

	@Override
	protected NacosRegistration getManagementRegistration() {
		return null;
	}

	@Override
	protected int getConfiguredPort() {
		return this.getPort().get();
	}

	@Override
	protected void setConfiguredPort(int port) {
		this.getPort().set(port);
	}

	@Override
	protected Object getConfiguration() {
		return null;
	}

	@Override
	protected boolean isEnabled() {
		return true;
	}

	/**
	 * Register the local service with the {@link ServiceRegistry}
	 */
	@Override
	protected void register() {
		this.registration.setPort(this.getPort().get());
		this.getServiceRegistry().register(getRegistration());
	}

	@Override
	@EventListener(EmbeddedServletContainerInitializedEvent.class)
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		if (context.equals(event.getApplicationContext())) {
			int localPort = event.getEmbeddedServletContainer().getPort();
			if (this.getPort().get() == 0) {
				LOGGER.info("Updating port to " + localPort);
				this.getPort().compareAndSet(0, localPort);
				start();
			}
		}
	}

}
