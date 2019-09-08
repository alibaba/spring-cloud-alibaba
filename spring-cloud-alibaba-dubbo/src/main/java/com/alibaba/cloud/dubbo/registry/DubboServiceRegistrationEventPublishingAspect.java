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
package com.alibaba.cloud.dubbo.registry;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.alibaba.cloud.dubbo.registry.event.ServiceInstancePreRegisteredEvent;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstanceRegisteredEvent;

/**
 * Dubbo Service Registration Event-Publishing Aspect
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServiceInstancePreRegisteredEvent
 * @see ServiceInstanceRegisteredEvent
 */
@Aspect
public class DubboServiceRegistrationEventPublishingAspect
		implements ApplicationEventPublisherAware {

	/**
	 * The pointcut expression for {@link ServiceRegistry#register(Registration)}
	 */
	public static final String REGISTER_POINTCUT_EXPRESSION = "execution(* org.springframework.cloud.client.serviceregistry.ServiceRegistry.register(*)) && args(registration)";

	private ApplicationEventPublisher applicationEventPublisher;

	@Before(REGISTER_POINTCUT_EXPRESSION)
	public void beforeRegister(Registration registration) {
		applicationEventPublisher
				.publishEvent(new ServiceInstancePreRegisteredEvent(registration));
	}

	@After(REGISTER_POINTCUT_EXPRESSION)
	public void afterRegister(Registration registration) {
		applicationEventPublisher
				.publishEvent(new ServiceInstanceRegisteredEvent(registration));
	}

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
