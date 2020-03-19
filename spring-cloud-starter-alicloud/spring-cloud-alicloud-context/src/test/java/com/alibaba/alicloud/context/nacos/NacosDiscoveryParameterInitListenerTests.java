/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.alicloud.context.nacos;

import com.alibaba.alicloud.context.BaseAliCloudSpringApplication;
import com.alibaba.alicloud.utils.ChangeOrderUtils;
import com.alibaba.cloud.context.ans.AliCloudAnsInitializer;
import com.alibaba.cloud.context.edas.EdasChangeOrderConfigurationFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * @author xiaolongzuo
 */
@PrepareForTest({ EdasChangeOrderConfigurationFactory.class,
		NacosDiscoveryParameterInitListener.class, AliCloudAnsInitializer.class })
public class NacosDiscoveryParameterInitListenerTests
		extends BaseAliCloudSpringApplication {

	@BeforeClass
	public static void setUp() {
		ChangeOrderUtils.mockChangeOrder();
	}

	@Test
	public void testNacosParameterInitListener() {
		assertThat(System.getProperty("spring.cloud.nacos.discovery.server-mode"))
				.isEqualTo("EDAS");
		assertThat(System.getProperty("spring.cloud.nacos.discovery.server-addr"))
				.isEqualTo("");
		assertThat(System.getProperty("spring.cloud.nacos.discovery.endpoint"))
				.isEqualTo("testDomain");
		assertThat(System.getProperty("spring.cloud.nacos.discovery.namespace"))
				.isEqualTo("testTenantId");
		assertThat(System.getProperty("spring.cloud.nacos.discovery.access-key"))
				.isEqualTo("testAK");
		assertThat(System.getProperty("spring.cloud.nacos.discovery.secret-key"))
				.isEqualTo("testSK");
		assertThat(System.getProperties().getProperty("nacos.naming.web.context"))
				.isEqualTo("/vipserver");
		assertThat(System.getProperties().getProperty("nacos.naming.exposed.port"))
				.isEqualTo("80");
	}

}
