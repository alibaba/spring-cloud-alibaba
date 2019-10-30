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

package com.alibaba.alicloud.context.ans;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;
import com.alibaba.alicloud.context.edas.EdasContextAutoConfiguration;
import com.alibaba.cloud.context.AliCloudServerMode;
import com.alibaba.cloud.context.ans.AliCloudAnsInitializer;
import com.alibaba.cloud.context.edas.AliCloudEdasSdk;

import com.aliyuncs.edas.model.v20170801.GetSecureTokenResponse;
import com.aliyuncs.edas.model.v20170801.InsertApplicationResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaolongzuo
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore("javax.management.*")
@SpringBootTest(classes = { AnsPropertiesLoadTests.TestConfiguration.class,
		AliCloudContextAutoConfiguration.class, EdasContextAutoConfiguration.class,
		AnsContextAutoConfiguration.class }, properties = {
				"spring.application.name=myapp", "spring.cloud.alicloud.access-key=ak",
				"spring.cloud.alicloud.secret-key=sk",
				"spring.cloud.alicloud.edas.namespace=cn-test",
				"spring.cloud.alicloud.ans.server-mode=EDAS",
				"spring.cloud.alicloud.ans.server-port=11111",
				"spring.cloud.alicloud.ans.server-list=10.10.10.10",
				"spring.cloud.alicloud.ans.client-domains=testDomain",
				"spring.cloud.alicloud.ans.client-weight=0.9",
				"spring.cloud.alicloud.ans.client-weights.testDomain=0.9",
				"spring.cloud.alicloud.oss.endpoint=test",
				"spring.cloud.alicloud.oss.enabled=false",
				"spring.cloud.alicloud.scx.enabled=false" })
@PrepareForTest({ AliCloudAnsInitializer.class })
public class AnsPropertiesLoadTests {

	@Autowired
	private AnsProperties ansProperties;

	@Test
	public void test() {
		assertThat(ansProperties.getServerMode()).isEqualTo(AliCloudServerMode.EDAS);
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
	}

	@Configuration
	@AutoConfigureBefore(EdasContextAutoConfiguration.class)
	public static class TestConfiguration {

		@Bean
		public AliCloudEdasSdk aliCloudEdasSdk() {
			GetSecureTokenResponse.SecureToken secureToken = new GetSecureTokenResponse.SecureToken();
			InsertApplicationResponse.ApplicationInfo applicationInfo = new InsertApplicationResponse.ApplicationInfo();
			applicationInfo.setAppId("testAppId");
			secureToken.setTenantId("testTenantId");
			secureToken.setAccessKey("testAK");
			secureToken.setSecretKey("testSK");
			secureToken.setAddressServerHost("testDomain");
			AliCloudEdasSdk aliCloudEdasSdk = Mockito.mock(AliCloudEdasSdk.class);
			Mockito.when(aliCloudEdasSdk.getSecureToken("cn-test"))
					.thenReturn(secureToken);
			Mockito.when(aliCloudEdasSdk.getApplicationInfo("myapp", "cn-test"))
					.thenReturn(applicationInfo);
			return aliCloudEdasSdk;
		}
	}
}
