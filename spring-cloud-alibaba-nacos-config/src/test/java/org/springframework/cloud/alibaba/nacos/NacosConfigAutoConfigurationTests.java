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

package org.springframework.cloud.alibaba.nacos;

import org.junit.Test;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceLocator;
import org.springframework.cloud.alibaba.nacos.refresh.NacosRefreshProperties;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 * @author pbting
 */
public class NacosConfigAutoConfigurationTests extends NacosPowerMockitBaseTests {
	@Test
	public void testNacosConfigProperties() {

		NacosConfigProperties nacosConfigProperties = context.getParent()
				.getBean(NacosConfigProperties.class);
		assertThat(nacosConfigProperties.getFileExtension()).isEqualTo("properties");
		assertThat(nacosConfigProperties.getPrefix() == null).isEqualTo(true);
		assertThat(nacosConfigProperties.getNamespace() == null).isEqualTo(true);
		assertThat(nacosConfigProperties.getName()).isEqualTo("sca-nacos-config");
		assertThat(nacosConfigProperties.getServerAddr()).isEqualTo("127.0.0.1:8848");
		assertThat(nacosConfigProperties.getEncode()).isEqualTo("utf-8");
		assertThat(nacosConfigProperties.getActiveProfiles())
				.isEqualTo(new String[] { "develop" });
		assertThat(nacosConfigProperties.getSharedDataids())
				.isEqualTo("base-common.properties,common.properties");
		assertThat(nacosConfigProperties.getRefreshableDataids())
				.isEqualTo("common.properties");
		assertThat(nacosConfigProperties.getExtConfig().size()).isEqualTo(3);
		assertThat(nacosConfigProperties.getExtConfig().get(0).getDataId())
				.isEqualTo("ext00.yaml");
		assertThat(nacosConfigProperties.getExtConfig().get(1).getGroup())
				.isEqualTo("EXT01_GROUP");
		assertThat(nacosConfigProperties.getExtConfig().get(1).isRefresh())
				.isEqualTo(true);
	}

	@Test
	public void nacosPropertySourceLocator() {
		NacosPropertySourceLocator nacosPropertySourceLocator = this.context
				.getBean(NacosPropertySourceLocator.class);
		PropertySource propertySource = nacosPropertySourceLocator
				.locate(this.context.getEnvironment());
		assertThat(propertySource instanceof CompositePropertySource).isEqualTo(true);
	}

	@Test
	public void testNacosRefreshProperties() {

		NacosRefreshProperties nacosRefreshProperties = this.context
				.getBean(NacosRefreshProperties.class);
		assertThat(nacosRefreshProperties.isEnabled()).isEqualTo(true);

	}

}
