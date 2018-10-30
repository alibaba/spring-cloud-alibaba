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

package org.springframework.cloud.alicloud.context;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * @author xiaolongzuo
 */
public class AliCloudPropertiesTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(AliCloudContextAutoConfiguration.class));

	@Test
	public void testConfigurationValueDefaultsAreAsExpected() {
		this.contextRunner.run(context -> {
			AliCloudProperties aliCloudProperties = context
					.getBean(AliCloudProperties.class);
			assertThat(aliCloudProperties.getAccessKey()).isNull();
			assertThat(aliCloudProperties.getSecretKey()).isNull();
		});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded() {
		this.contextRunner.withPropertyValues("spring.cloud.alicloud.access-key=123",
				"spring.cloud.alicloud.secret-key=123456").run(context -> {
					AliCloudProperties aliCloudProperties = context
							.getBean(AliCloudProperties.class);
					assertThat(aliCloudProperties.getAccessKey()).isEqualTo("123");
					assertThat(aliCloudProperties.getSecretKey()).isEqualTo("123456");
				});
	}

}
