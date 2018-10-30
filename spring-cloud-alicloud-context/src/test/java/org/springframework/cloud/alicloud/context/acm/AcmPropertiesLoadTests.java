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
@SpringBootTest(classes = { AcmContextBootstrapConfiguration.class }, properties = {
		"spring.application.name=myapp", "spring.cloud.alicloud.access-key=ak",
		"spring.cloud.alicloud.secret-key=sk",
		"spring.cloud.alicloud.acm.server-mode=EDAS",
		"spring.cloud.alicloud.acm.server-port=11111",
		"spring.cloud.alicloud.acm.server-list=10.10.10.10",
		"spring.cloud.alicloud.acm.namespace=testNamespace",
		"spring.cloud.alicloud.acm.endpoint=testDomain",
		"spring.cloud.alicloud.acm.group=testGroup",
		"spring.cloud.alicloud.acm.file-extension=yaml" })
public class AcmPropertiesLoadTests {

	@Autowired
	private AcmProperties acmProperties;

	@Test
	public void test() {
		assertThat(acmProperties.getServerMode()).isEqualTo(AliCloudServerMode.EDAS);
		assertThat(acmProperties.getServerList()).isEqualTo("10.10.10.10");
		assertThat(acmProperties.getServerPort()).isEqualTo("11111");
		assertThat(acmProperties.getEndpoint()).isEqualTo("testDomain");
		assertThat(acmProperties.getGroup()).isEqualTo("testGroup");
		assertThat(acmProperties.getFileExtension()).isEqualTo("yaml");
		assertThat(acmProperties.getNamespace()).isEqualTo("testNamespace");
	}
}
