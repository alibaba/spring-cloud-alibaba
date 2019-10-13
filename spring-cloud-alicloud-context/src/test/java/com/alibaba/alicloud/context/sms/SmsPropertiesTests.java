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

package com.alibaba.alicloud.context.sms;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;
import com.alibaba.alicloud.context.edas.EdasContextAutoConfiguration;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author xiaolongzuo
 */
public class SmsPropertiesTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SmsContextAutoConfiguration.class,
					EdasContextAutoConfiguration.class,
					AliCloudContextAutoConfiguration.class));

	@Test
	public void testConfigurationValueDefaultsAreAsExpected() {
		this.contextRunner.run(context -> {
			SmsProperties config = context.getBean(SmsProperties.class);
			assertThat(config.getReportQueueName()).isNull();
			assertThat(config.getUpQueueName()).isNull();
			assertThat(config.getConnectTimeout()).isEqualTo("10000");
			assertThat(config.getReadTimeout()).isEqualTo("10000");
		});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded() {
		this.contextRunner
				.withPropertyValues("spring.cloud.alicloud.sms.reportQueueName=q1",
						"spring.cloud.alicloud.sms.upQueueName=q2",
						"spring.cloud.alicloud.sms.connect-timeout=20",
						"spring.cloud.alicloud.sms.read-timeout=30")
				.run(context -> {
					SmsProperties config = context.getBean(SmsProperties.class);
					assertThat(config.getReportQueueName()).isEqualTo("q1");
					assertThat(config.getUpQueueName()).isEqualTo("q2");
					assertThat(config.getConnectTimeout()).isEqualTo("20");
					assertThat(config.getReadTimeout()).isEqualTo("30");
				});
	}

}
