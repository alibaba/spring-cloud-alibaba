/*
 * Copyright (C) 2018 the original author or authors.
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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.springframework.context.ApplicationEvent;

/**
 * {@link ApplicationEvent Event} raised when the subscribed services are changed
 * <p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEvent
 */
public class SubscribedServicesChangedEvent extends ApplicationEvent {

	private final Set<String> oldSubscribedServices;

	private final Set<String> newSubscribedServices;

	private final boolean changed;

	/**
	 * Create a new ApplicationEvent.
	 *
	 * @param source the object on which the event initially occurred (never {@code null})
	 * @param oldSubscribedServices the subscribed services before changed
	 * @param newSubscribedServices the subscribed services after changed
	 */
	public SubscribedServicesChangedEvent(Object source,
			Set<String> oldSubscribedServices, Set<String> newSubscribedServices) {
		super(source);
		this.oldSubscribedServices = new LinkedHashSet<>(oldSubscribedServices);
		this.newSubscribedServices = new LinkedHashSet<>(newSubscribedServices);
		this.changed = !Objects.equals(oldSubscribedServices, newSubscribedServices);
	}

	public Set<String> getOldSubscribedServices() {
		return oldSubscribedServices;
	}

	public Set<String> getNewSubscribedServices() {
		return newSubscribedServices;
	}

	public boolean isChanged() {
		return changed;
	}
}
