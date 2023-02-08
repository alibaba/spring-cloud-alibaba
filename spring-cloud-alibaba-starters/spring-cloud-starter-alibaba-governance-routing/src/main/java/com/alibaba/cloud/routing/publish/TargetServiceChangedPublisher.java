/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.publish;

import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.cloud.commons.governance.event.TargetServiceChangedEvent;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 * @since 2.2.10-RC1
 */
public class TargetServiceChangedPublisher implements ApplicationContextAware {

	private ConcurrentHashMap<String, Object> targetServiceMap = new ConcurrentHashMap<String, Object>();

	private final Object object = new Object();

	public void addTargetService(String targetService) {
		Object value = targetServiceMap.putIfAbsent(targetService, object);
		if (value == null && applicationContext != null) {
			applicationContext.publishEvent(new TargetServiceChangedEvent(targetService));
		}
	}

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}

}
