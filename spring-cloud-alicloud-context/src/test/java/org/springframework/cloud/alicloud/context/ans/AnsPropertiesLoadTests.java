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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.cloud.context.AliCloudServerMode;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AnsContextAutoConfiguration.class }, properties = {
		"spring.cloud.alicloud.ans.server-mode=EDAS",
		"spring.cloud.alicloud.ans.server-port=11111",
		"spring.cloud.alicloud.ans.server-list=10.10.10.10",
		"spring.cloud.alicloud.ans.client-domains=testDomain",
		"spring.cloud.alicloud.ans.client-weight=0.9",
		"spring.cloud.alicloud.ans.client-weights.testDomain=0.9" })
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
}
