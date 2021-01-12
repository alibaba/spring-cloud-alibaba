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

package com.alibaba.cloud.dubbo.bootstrap;

import com.alibaba.cloud.dubbo.bootstrap.event.DubboBootstrapStartedEvent;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;

/**
 * publish Dubbo microsystem startup finish event.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */

@Component
public class DubboBootstrapStartCommandLineRunner
		implements CommandLineRunner, ApplicationEventPublisherAware {

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void run(String... args) {
		applicationEventPublisher.publishEvent(
				new DubboBootstrapStartedEvent(DubboBootstrapWrapper.getInstance()));
	}

}
