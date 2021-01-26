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

package com.alibaba.cloud.dubbo.registry.event;

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationEvent;

/**
 * The
 * before-{@link org.springframework.cloud.client.serviceregistry.ServiceRegistry#register(org.springframework.cloud.client.serviceregistry.Registration)
 * register} event for {@link org.springframework.cloud.client.ServiceInstance}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class ServiceInstancePreDeregisteredEvent extends ApplicationEvent {

	private final ServiceRegistry<Registration> registry;

	public ServiceInstancePreDeregisteredEvent(ServiceRegistry<Registration> registry,
			Registration source) {
		super(source);
		this.registry = registry;
	}

	@Override
	public Registration getSource() {
		return (Registration) super.getSource();
	}

	public ServiceRegistry<Registration> getRegistry() {
		return registry;
	}

}
