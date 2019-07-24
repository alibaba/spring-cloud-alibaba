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

package com.alibaba.alicloud.context;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.alicloud.context.ans.AnsContextAutoConfiguration;
import com.alibaba.alicloud.context.edas.EdasContextAutoConfiguration;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AliCloudContextAutoConfiguration.class,
		EdasContextAutoConfiguration.class,
		AnsContextAutoConfiguration.class }, properties = {
				"spring.cloud.alicloud.access-key=123",
				"spring.cloud.alicloud.secret-key=123456" })
public class AliCloudPropertiesLoadTests {

	@Autowired
	private AliCloudProperties aliCloudProperties;

	@Test
	public void test() {
		assertThat(aliCloudProperties.getAccessKey()).isEqualTo("123");
		assertThat(aliCloudProperties.getSecretKey()).isEqualTo("123456");
	}

}
