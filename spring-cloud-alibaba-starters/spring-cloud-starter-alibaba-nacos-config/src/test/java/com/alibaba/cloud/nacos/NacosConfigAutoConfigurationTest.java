/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.nacos;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosConfigAutoConfiguration Tester.
 *
 * @author freeman
 */
public class NacosConfigAutoConfigurationTest {

	@Test
	public void noImports_thenCreateProperties() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NacosConfigAutoConfiguration.class);

		assertThat(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
				NacosConfigProperties.class).length).isEqualTo(1);
		assertThat(context.getBean(NacosConfigProperties.class).getServerAddr())
				.isEqualTo("127.0.0.1:8848");
		context.close();
	}

	@Test
	public void imports_thenNoCreateProperties() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NacosConfigAutoConfiguration.class);
		// mock import
		context.registerBean(NacosConfigProperties.class, () -> {
			NacosConfigProperties properties = new NacosConfigProperties();
			properties.setServerAddr("localhost");
			return properties;
		});

		assertThat(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
				NacosConfigProperties.class).length).isEqualTo(1);
		assertThat(context.getBean(NacosConfigProperties.class).getServerAddr())
				.isEqualTo("localhost");
		context.close();
	}

}
