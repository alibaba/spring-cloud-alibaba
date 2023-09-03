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

package com.alibaba.cloud.mtls.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class PreparedEventRecorder
		implements ApplicationListener<ApplicationPreparedEvent> {

	private static final Log log = LogFactory.getLog(PreparedEventRecorder.class);

	private static ConfigurableApplicationContext context;

	private static SpringApplication application;

	private static String[] args;

	@Override
	public void onApplicationEvent(ApplicationPreparedEvent input) {
		synchronized (PreparedEventRecorder.class) {
			context = input.getApplicationContext();
			args = input.getArgs();
			application = input.getSpringApplication();
			application.addInitializers(new PostProcessorInitializer());
		}
	}

	static ConfigurableApplicationContext getContext() {
		return context;
	}

	static SpringApplication getApplication() {
		return application;
	}

	static String[] getArgs() {
		return args;
	}

	class PostProcessorInitializer
			implements ApplicationContextInitializer<GenericApplicationContext> {

		@Override
		public void initialize(GenericApplicationContext context) {
			context.registerBean(PostProcessor.class, () -> new PostProcessor());
		}

	}

	class PostProcessor implements BeanPostProcessor {

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName)
				throws BeansException {
			if (bean instanceof PreparedEventRecorder) {
				return PreparedEventRecorder.this;
			}
			return bean;
		}

	}

}
