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

package com.alibaba.alicloud.context.edas;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;

/**
 * @author xiaolongzuo
 */
public class EdasPropertiesTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(EdasContextAutoConfiguration.class,
					AliCloudContextAutoConfiguration.class));

	@Test
	public void testConfigurationValueDefaultsAreAsExpected() {
		this.contextRunner.withPropertyValues().run(context -> {
			EdasProperties edasProperties = context.getBean(EdasProperties.class);
			assertThat(edasProperties.getNamespace()).isNull();
			assertThat(edasProperties.isApplicationNameValid()).isFalse();
		});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded1() {
		this.contextRunner
				.withPropertyValues("spring.cloud.alicloud.edas.namespace=testns",
						"spring.application.name=myapps")
				.run(context -> {
					EdasProperties edasProperties = context.getBean(EdasProperties.class);
					assertThat(edasProperties.getNamespace()).isEqualTo("testns");
					assertThat(edasProperties.getApplicationName()).isEqualTo("myapps");
				});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded2() {
		this.contextRunner
				.withPropertyValues("spring.cloud.alicloud.edas.namespace=testns",
						"spring.cloud.alicloud.edas.application.name=myapps")
				.run(context -> {
					EdasProperties edasProperties = context.getBean(EdasProperties.class);
					assertThat(edasProperties.getNamespace()).isEqualTo("testns");
					assertThat(edasProperties.getApplicationName()).isEqualTo("myapps");
				});
	}

}
