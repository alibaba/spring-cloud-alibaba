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
		"spring.application.name=myapp" })
public class AcmPropertiesDefaultTests {

	@Autowired
	private AcmProperties acmProperties;

	@Test
	public void test() {
		assertThat(acmProperties.getServerMode()).isEqualTo(AliCloudServerMode.LOCAL);
		assertThat(acmProperties.getServerList()).isEqualTo("127.0.0.1");
		assertThat(acmProperties.getServerPort()).isEqualTo("8080");
		assertThat(acmProperties.getEndpoint()).isNull();
		assertThat(acmProperties.getFileExtension()).isEqualTo("properties");
		assertThat(acmProperties.getGroup()).isEqualTo("DEFAULT_GROUP");
		assertThat(acmProperties.getNamespace()).isNull();
		assertThat(acmProperties.getRamRoleName()).isNull();
		assertThat(acmProperties.getTimeout()).isEqualTo(3000);
	}
}
