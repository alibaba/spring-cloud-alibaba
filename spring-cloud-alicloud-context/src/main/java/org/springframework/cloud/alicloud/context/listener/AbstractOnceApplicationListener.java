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

package org.springframework.cloud.alicloud.context.listener;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * @author xiaolongzuo
 */
public abstract class AbstractOnceApplicationListener<T extends ApplicationEvent>
		implements ApplicationListener<T> {

	private static final String BOOTSTRAP_CONFIG_NAME_VALUE = "bootstrap";

	private static final String BOOTSTRAP_CONFIG_NAME_KEY = "spring.config.name";

	private static ConcurrentHashMap<Class<?>, AtomicBoolean> lockMap = new ConcurrentHashMap<>();

	@Override
	public void onApplicationEvent(T event) {
		if (event instanceof ApplicationContextEvent) {
			ApplicationContext applicationContext = ((ApplicationContextEvent) event)
					.getApplicationContext();
			// skip bootstrap context or super parent context.
			if (applicationContext.getParent() == null
					|| BOOTSTRAP_CONFIG_NAME_VALUE.equals(applicationContext
							.getEnvironment().getProperty(BOOTSTRAP_CONFIG_NAME_KEY))) {
				return;
			}
		}
		Class<?> clazz = getClass();
		lockMap.putIfAbsent(clazz, new AtomicBoolean(false));
		AtomicBoolean handled = lockMap.get(clazz);
		// only execute once.
		if (!handled.compareAndSet(false, true)) {
			return;
		}
		if (conditionalOnClass() != null) {
			try {
				Class.forName(conditionalOnClass());
			}
			catch (ClassNotFoundException e) {
				// ignored
				return;
			}
		}
		handleEvent(event);
	}

	/**
	 * handle event.
	 *
	 * @param event
	 */
	protected abstract void handleEvent(T event);

	/**
	 * condition on class.
	 *
	 * @return
	 */
	protected String conditionalOnClass() {
		return null;
	}

}
