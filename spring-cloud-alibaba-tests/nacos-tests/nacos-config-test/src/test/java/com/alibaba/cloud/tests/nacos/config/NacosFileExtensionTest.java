/*
 * Copyright 2013-2022 the original author or authors.
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

package com.alibaba.cloud.tests.nacos.config;

import java.util.Properties;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosFileExtensionTest.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=test-name",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.username=nacos",
		"spring.cloud.nacos.config.password=nacos",
		"spring.cloud.nacos.config.file-extension=properties",
		"spring.cloud.bootstrap.enabled=true"})

public class NacosFileExtensionTest {

	/**
	 * nacos upload conf file.
	 */
	public static final String YAML_CONTENT = "configdata:\n" + "  user:\n"
			+ "    age: 22\n" + "    name: freeman1123\n" + "    map:\n"
			+ "      hobbies:\n" + "        - art\n" + "        - programming\n"
			+ "        - movie\n" + "      intro: Hello, I'm freeman\n"
			+ "      extra: yo~\n" + "    users:\n" + "      - name: dad\n"
			+ "        age: 20\n" + "      - name: mom\n" + "        age: 18";

	@Autowired
	private NacosConfigProperties nacosConfigProperties;

	private ConfigService remoteService;

	private NacosConfigManager nacosConfigManager;

	@BeforeAll
	public static void setUp() {

	}

	@BeforeEach
	public void prepare() throws NacosException {
		Properties nacosSettings = new Properties();
		String serverAddress = "127.0.0.1:8848";
		nacosSettings.put(PropertyKeyConst.SERVER_ADDR, serverAddress);
		nacosSettings.put(PropertyKeyConst.USERNAME, "nacos");
		nacosSettings.put(PropertyKeyConst.PASSWORD, "nacos");

		remoteService = ConfigFactory.createConfigService(nacosSettings);
		nacosConfigManager = new NacosConfigManager(nacosConfigProperties);
	}

	@Test
	public void contextLoads() throws NacosException, InterruptedException {
		ConfigService localService = nacosConfigManager.getConfigService();
		updateConfig();
		Thread.sleep(2000L);
		String localContent = fetchConfig(localService, "nacos-config-refresh.yml",
				"DEFAULT_GROUP", TIME_OUT);
		String remoteContent = fetchConfig(remoteService, "nacos-config-refresh.yml",
				"DEFAULT_GROUP", TIME_OUT);
		Assertions.assertEquals(localContent, remoteContent);

		assertThat(nacosConfigProperties.getServerAddr()).isEqualTo("127.0.0.1:8848");
		assertThat(nacosConfigProperties.getFileExtension()).isEqualTo("properties");
		assertThat(nacosConfigProperties.getUsername()).isEqualTo("nacos");

	}

	private String fetchConfig(ConfigService configService, String dataId, String group, long timeoutMs) throws NacosException {
		return configService.getConfig(dataId, group, timeoutMs);
	}

	private void updateConfig() throws NacosException {
		remoteService.publishConfig("nacos-config-refresh.yml", "DEFAULT_GROUP",
				YAML_CONTENT, "yaml");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class})
	public static class TestConfig {

	}
}
