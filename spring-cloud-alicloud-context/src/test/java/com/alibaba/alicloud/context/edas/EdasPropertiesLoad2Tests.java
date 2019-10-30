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

package com.alibaba.alicloud.context.edas;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;
import com.alibaba.alicloud.context.ans.AnsContextAutoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AliCloudContextAutoConfiguration.class,
		EdasContextAutoConfiguration.class,
		AnsContextAutoConfiguration.class }, properties = {
				"spring.cloud.alicloud.edas.namespace=testns",
				"spring.cloud.alicloud.edas.application.name=myapps" })
public class EdasPropertiesLoad2Tests {

	@Autowired
	private EdasProperties edasProperties;

	@Test
	public void test() {
		assertThat(edasProperties.getNamespace()).isEqualTo("testns");
		assertThat(edasProperties.getApplicationName()).isEqualTo("myapps");
	}
}
