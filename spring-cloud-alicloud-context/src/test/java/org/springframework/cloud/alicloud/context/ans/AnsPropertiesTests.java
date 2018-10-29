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

package org.springframework.cloud.alicloud.context.ans;

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
public class AnsPropertiesTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(AnsContextAutoConfiguration.class,
					EdasContextAutoConfiguration.class,
					AliCloudContextAutoConfiguration.class));

	@Test
	public void testConfigurationValueDefaultsAreAsExpected()
			throws ClassNotFoundException {
		this.contextRunner.withPropertyValues().run(context -> {
			AnsProperties config = context.getBean(AnsProperties.class);
			assertThat(config.getServerMode()).isEqualTo(AliCloudServerMode.LOCAL);
			assertThat(config.getServerList()).isEqualTo("127.0.0.1");
			assertThat(config.getServerPort()).isEqualTo("8080");
			assertThat(config.getClientDomains()).isEqualTo("");
			assertThat(config.getClientWeight()).isEqualTo(1.0F);
			assertThat(config.getClientWeights().size()).isEqualTo(0);
			assertThat(config.getClientTokens().size()).isEqualTo(0);
			assertThat(config.getClientMetadata().size()).isEqualTo(0);
			assertThat(config.getClientToken()).isNull();
			assertThat(config.getClientCluster()).isEqualTo("DEFAULT");
			assertThat(config.isRegisterEnabled()).isTrue();
			assertThat(config.getClientInterfaceName()).isNull();
			assertThat(config.getClientPort()).isEqualTo(-1);
			assertThat(config.getEnv()).isEqualTo("DEFAULT");
			assertThat(config.isSecure()).isFalse();
			assertThat(config.getTags().size()).isEqualTo(1);
			assertThat(config.getTags().keySet().iterator().next())
					.isEqualTo("ANS_SERVICE_TYPE");
			assertThat(config.getTags().get("ANS_SERVICE_TYPE"))
					.isEqualTo("SPRING_CLOUD");
		});
	}

	@Test
	public void testConfigurationValuesAreCorrectlyLoaded() {
		this.contextRunner
				.withPropertyValues("spring.cloud.alicloud.ans.server-mode=EDAS",
						"spring.cloud.alicloud.ans.server-port=11111",
						"spring.cloud.alicloud.ans.server-list=10.10.10.10",
						"spring.cloud.alicloud.ans.client-domains=testDomain",
						"spring.cloud.alicloud.ans.client-weight=0.9",
						"spring.cloud.alicloud.ans.client-weights.testDomain=0.9")
				.run(context -> {
					AnsProperties config = context.getBean(AnsProperties.class);
					assertThat(config.getServerMode()).isEqualTo(AliCloudServerMode.EDAS);
					assertThat(config.getServerList()).isEqualTo("10.10.10.10");
					assertThat(config.getServerPort()).isEqualTo("11111");
					assertThat(config.getClientDomains()).isEqualTo("testDomain");
					assertThat(config.getClientWeight()).isEqualTo(0.9F);
					assertThat(config.getClientWeights().size()).isEqualTo(1);
					assertThat(config.getClientTokens().size()).isEqualTo(0);
					assertThat(config.getClientMetadata().size()).isEqualTo(0);
					assertThat(config.getClientToken()).isNull();
					assertThat(config.getClientCluster()).isEqualTo("DEFAULT");
					assertThat(config.isRegisterEnabled()).isTrue();
					assertThat(config.getClientInterfaceName()).isNull();
					assertThat(config.getClientPort()).isEqualTo(-1);
					assertThat(config.getEnv()).isEqualTo("DEFAULT");
					assertThat(config.isSecure()).isFalse();
					assertThat(config.getTags().size()).isEqualTo(1);
					assertThat(config.getTags().keySet().iterator().next())
							.isEqualTo("ANS_SERVICE_TYPE");
					assertThat(config.getTags().get("ANS_SERVICE_TYPE"))
							.isEqualTo("SPRING_CLOUD");
				});
	}

}
