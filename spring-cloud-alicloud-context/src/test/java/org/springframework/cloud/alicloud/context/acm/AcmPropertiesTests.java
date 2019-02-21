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

package org.springframework.cloud.alicloud.context.acm;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.alicloud.context.AliCloudContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasContextAutoConfiguration;

import com.alibaba.cloud.context.AliCloudServerMode;

/**
 * @author xiaolongzuo
 */
public class AcmPropertiesTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(
					AutoConfigurations.of(AcmContextBootstrapConfiguration.class,
							EdasContextAutoConfiguration.class,
							AliCloudContextAutoConfiguration.class));

	@Test
	public void testConfigurationValueDefaultsAreAsExpected() {
		this.contextRunner.withPropertyValues("spring.application.name=myapp")
				.run(context -> {
					AcmProperties config = context.getBean(AcmProperties.class);
					assertThat(config.getServerMode())
							.isEqualTo(AliCloudServerMode.LOCAL);
					assertThat(config.getServerList()).isEqualTo("127.0.0.1");
					assertThat(config.getServerPort()).isEqualTo("8080");
					assertThat(config.getEndpoint()).isNull();
					assertThat(config.getFileExtension()).isEqualTo("properties");
					assertThat(config.getGroup()).isEqualTo("DEFAULT_GROUP");
					assertThat(config.getNamespace()).isNull();
					assertThat(config.getRamRoleName()).isNull();
					assertThat(config.getTimeout()).isEqualTo(3000);
				});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded() {
		this.contextRunner.withPropertyValues("spring.application.name=myapp",
				"spring.cloud.alicloud.access-key=ak",
				"spring.cloud.alicloud.secret-key=sk",
				"spring.cloud.alicloud.acm.server-mode=EDAS",
				"spring.cloud.alicloud.acm.server-port=11111",
				"spring.cloud.alicloud.acm.server-list=10.10.10.10",
				"spring.cloud.alicloud.acm.namespace=testNamespace",
				"spring.cloud.alicloud.acm.endpoint=testDomain",
				"spring.cloud.alicloud.acm.group=testGroup",
				"spring.cloud.alicloud.acm.file-extension=yaml").run(context -> {
					AcmProperties acmProperties = context.getBean(AcmProperties.class);
					assertThat(acmProperties.getServerMode())
							.isEqualTo(AliCloudServerMode.EDAS);
					assertThat(acmProperties.getServerList()).isEqualTo("10.10.10.10");
					assertThat(acmProperties.getServerPort()).isEqualTo("11111");
					assertThat(acmProperties.getEndpoint()).isEqualTo("testDomain");
					assertThat(acmProperties.getGroup()).isEqualTo("testGroup");
					assertThat(acmProperties.getFileExtension()).isEqualTo("yaml");
					assertThat(acmProperties.getNamespace()).isEqualTo("testNamespace");
				});
	}

	@Test
	public void testAcmIntegrationConfigurationValuesAreCorrectlyLoaded() {
		this.contextRunner.withPropertyValues("spring.application.name=myapp",
				"spring.application.group=com.alicloud.test",
				"spring.cloud.alicloud.access-key=ak",
				"spring.cloud.alicloud.secret-key=sk",
				"spring.cloud.alicloud.acm.server-mode=EDAS",
				"spring.cloud.alicloud.acm.server-port=11111",
				"spring.cloud.alicloud.acm.server-list=10.10.10.10",
				"spring.cloud.alicloud.acm.namespace=testNamespace",
				"spring.cloud.alicloud.acm.endpoint=testDomain",
				"spring.cloud.alicloud.acm.group=testGroup",
				"spring.cloud.alicloud.acm.file-extension=yaml").run(context -> {
					AcmIntegrationProperties acmIntegrationProperties = context
							.getBean(AcmIntegrationProperties.class);
					assertThat(acmIntegrationProperties.getGroupConfigurationDataIds()
							.size()).isEqualTo(2);
					assertThat(acmIntegrationProperties
							.getApplicationConfigurationDataIds().size()).isEqualTo(2);
				});
	}

	@Test
	public void testAcmIntegrationConfigurationValuesAreCorrectlyLoaded2() {
		this.contextRunner.withPropertyValues("spring.application.name=myapp",
				"spring.application.group=com.alicloud.test",
				"spring.profiles.active=profile1,profile2",
				"spring.cloud.alicloud.access-key=ak",
				"spring.cloud.alicloud.secret-key=sk",
				"spring.cloud.alicloud.acm.server-mode=EDAS",
				"spring.cloud.alicloud.acm.server-port=11111",
				"spring.cloud.alicloud.acm.server-list=10.10.10.10",
				"spring.cloud.alicloud.acm.namespace=testNamespace",
				"spring.cloud.alicloud.acm.endpoint=testDomain",
				"spring.cloud.alicloud.acm.group=testGroup",
				"spring.cloud.alicloud.acm.file-extension=yaml").run(context -> {
					AcmIntegrationProperties acmIntegrationProperties = context
							.getBean(AcmIntegrationProperties.class);
					assertThat(acmIntegrationProperties.getGroupConfigurationDataIds()
							.size()).isEqualTo(2);
					assertThat(acmIntegrationProperties
							.getApplicationConfigurationDataIds().size()).isEqualTo(6);
				});
	}

}
