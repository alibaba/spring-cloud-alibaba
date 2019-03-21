/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context.scx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alicloud.context.AliCloudContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.ans.AnsContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AliCloudContextAutoConfiguration.class,
		EdasContextAutoConfiguration.class, AnsContextAutoConfiguration.class,
		ScxContextAutoConfiguration.class }, properties = {
				"spring.cloud.alicloud.scx.group-id=1-2-3-4",
				"spring.cloud.alicloud.edas.namespace=cn-test" })
public class ScxPropertiesLoadTests {

	@Autowired
	private EdasProperties edasProperties;

	@Autowired
	private ScxProperties scxProperties;

	@Test
	public void testSxcProperties() {
		assertThat(scxProperties.getGroupId()).isEqualTo("1-2-3-4");
		assertThat(edasProperties.getNamespace()).isEqualTo("cn-test");
		assertThat(scxProperties.getDomainName()).isNull();
	}

}
