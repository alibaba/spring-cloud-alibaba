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

package org.springframework.cloud.alicloud.context.nacos;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.cloud.alicloud.context.BaseAliCloudSpringApplication;
import org.springframework.cloud.alicloud.utils.ChangeOrderUtils;

import com.alibaba.cloud.context.ans.AliCloudAnsInitializer;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;

/**
 * @author xiaolongzuo
 */
@PrepareForTest({ EdasChangeOrderConfigurationFactory.class,
		NacosParameterInitListener.class, AliCloudAnsInitializer.class })
public class NacosParameterInitListenerTests extends BaseAliCloudSpringApplication {

	@BeforeClass
	public static void setUp() {
		ChangeOrderUtils.mockChangeOrder();
	}

	@Test
	public void testNacosParameterInitListener() {
		assertThat(System.getProperty("spring.cloud.nacos.config.server-mode"))
				.isEqualTo("EDAS");
		assertThat(System.getProperty("spring.cloud.nacos.config.server-addr"))
				.isEqualTo("");
		assertThat(System.getProperty("spring.cloud.nacos.config.endpoint"))
				.isEqualTo("testDomain");
		assertThat(System.getProperty("spring.cloud.nacos.config.namespace"))
				.isEqualTo("testTenantId");
		assertThat(System.getProperty("spring.cloud.nacos.config.access-key"))
				.isEqualTo("testAK");
		assertThat(System.getProperty("spring.cloud.nacos.config.secret-key"))
				.isEqualTo("testSK");

	}
}
