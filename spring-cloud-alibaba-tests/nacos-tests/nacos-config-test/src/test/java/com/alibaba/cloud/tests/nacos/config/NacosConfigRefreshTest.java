/*
 * Copyright 2013-2023 the original author or authors.
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

import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import com.alibaba.cloud.testsupport.Tester;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * Test function: Nacos config refresh.
 *
 * @author freeman
 */
// @HasDockerAndItEnabled
@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
public class NacosConfigRefreshTest {

	/**
	 * nacos upload conf file.
	 */
	public static final String YAML_CONTENT = "configdata:\n" + "  user:\n"
			+ "    age: 22\n" + "    name: freeman1123\n" + "    map:\n"
			+ "      hobbies:\n" + "        - art\n" + "        - programming\n"
			+ "        - movie\n" + "      intro: Hello, I'm freeman\n"
			+ "      extra: yo~\n" + "    users:\n" + "      - name: dad\n"
			+ "        age: 20\n" + "      - name: mom\n" + "        age: 18";

	@Mock
	protected ConfigService configService;

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

		configService = ConfigFactory.createConfigService(nacosSettings);

	}

	@Test
	public void testRefreshConfig() throws InterruptedException {
		Thread.sleep(2000L);
		Tester.testFunction("Dynamic refresh config", () -> {
			// update config
			updateConfig();

			// wait config refresh
			Thread.sleep(2000L);
			String content = configService.getConfig("nacos-config-refresh.yml",
					"DEFAULT_GROUP", TIME_OUT);

			assertThat(content).isEqualTo(YAML_CONTENT);
		});
	}

	private void updateConfig() throws NacosException {
		configService.publishConfig("nacos-config-refresh.yml", "DEFAULT_GROUP", YAML_CONTENT,
				"yaml");
	}
}
