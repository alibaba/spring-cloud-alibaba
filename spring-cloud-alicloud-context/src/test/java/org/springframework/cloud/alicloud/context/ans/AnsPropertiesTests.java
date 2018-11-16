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
			AnsProperties ansProperties = context.getBean(AnsProperties.class);
			assertThat(ansProperties.getServerMode()).isEqualTo(AliCloudServerMode.LOCAL);
			assertThat(ansProperties.getServerList()).isEqualTo("127.0.0.1");
			assertThat(ansProperties.getServerPort()).isEqualTo("8080");
			assertThat(ansProperties.getClientDomains()).isEqualTo("");
			assertThat(ansProperties.getClientWeight()).isEqualTo(1.0F);
			assertThat(ansProperties.getClientWeights().size()).isEqualTo(0);
			assertThat(ansProperties.getClientTokens().size()).isEqualTo(0);
			assertThat(ansProperties.getClientMetadata().size()).isEqualTo(0);
			assertThat(ansProperties.getClientToken()).isNull();
			assertThat(ansProperties.getClientCluster()).isEqualTo("DEFAULT");
			assertThat(ansProperties.isRegisterEnabled()).isTrue();
			assertThat(ansProperties.getClientInterfaceName()).isNull();
			assertThat(ansProperties.getClientPort()).isEqualTo(-1);
			assertThat(ansProperties.getEnv()).isEqualTo("DEFAULT");
			assertThat(ansProperties.isSecure()).isFalse();
			assertThat(ansProperties.getTags().size()).isEqualTo(1);
			assertThat(ansProperties.getTags().keySet().iterator().next())
					.isEqualTo("ANS_SERVICE_TYPE");
			assertThat(ansProperties.getTags().get("ANS_SERVICE_TYPE"))
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
					AnsProperties ansProperties = context.getBean(AnsProperties.class);
					assertThat(ansProperties.getServerMode())
							.isEqualTo(AliCloudServerMode.EDAS);
					assertThat(ansProperties.getServerList()).isEqualTo("10.10.10.10");
					assertThat(ansProperties.getServerPort()).isEqualTo("11111");
					assertThat(ansProperties.getClientDomains()).isEqualTo("testDomain");
					assertThat(ansProperties.getClientWeight()).isEqualTo(0.9F);
					assertThat(ansProperties.getClientWeights().size()).isEqualTo(1);
					assertThat(ansProperties.getClientTokens().size()).isEqualTo(0);
					assertThat(ansProperties.getClientMetadata().size()).isEqualTo(0);
					assertThat(ansProperties.getClientToken()).isNull();
					assertThat(ansProperties.getClientCluster()).isEqualTo("DEFAULT");
					assertThat(ansProperties.isRegisterEnabled()).isTrue();
					assertThat(ansProperties.getClientInterfaceName()).isNull();
					assertThat(ansProperties.getClientPort()).isEqualTo(-1);
					assertThat(ansProperties.getEnv()).isEqualTo("DEFAULT");
					assertThat(ansProperties.isSecure()).isFalse();
					assertThat(ansProperties.getTags().size()).isEqualTo(1);
					assertThat(ansProperties.getTags().keySet().iterator().next())
							.isEqualTo("ANS_SERVICE_TYPE");
					assertThat(ansProperties.getTags().get("ANS_SERVICE_TYPE"))
							.isEqualTo("SPRING_CLOUD");
				});
	}

}
