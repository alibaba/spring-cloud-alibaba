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
@SpringBootTest(classes = { AnsContextAutoConfiguration.class })
public class AnsPropertiesDefaultTests {

	@Autowired
	private AnsProperties ansProperties;

	@Test
	public void test() {
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
	}
}
