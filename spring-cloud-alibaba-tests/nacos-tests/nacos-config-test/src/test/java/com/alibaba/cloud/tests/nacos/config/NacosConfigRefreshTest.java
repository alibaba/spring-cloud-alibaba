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

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.testsupport.ContainerStarter;
import com.alibaba.cloud.testsupport.HasDockerAndItEnabled;
import com.alibaba.cloud.testsupport.Tester;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static com.alibaba.cloud.tests.nacos.config.NacosConfigRefreshTest.PushConfigConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 *
 * @author freeman
 */
@HasDockerAndItEnabled
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {
		"spring.profiles.active=nacos-config-refresh"
})
@Import(PushConfigConfiguration.class)
public class NacosConfigRefreshTest {

	static GenericContainer nacos = ContainerStarter.startNacos();

	private static final String serverAddr;

	static {
		serverAddr = "localhost:" + nacos.getMappedPort(8848);
		System.setProperty("spring.cloud.nacos.config.server-addr", serverAddr);
	}

	@Autowired
	private NacosConfigManager nacosConfigManager;
	@Autowired
	private UserProperties userProperties;

	@Test
	public void testRefreshConfig() throws InterruptedException {
		// make sure everything is ready !
		Thread.sleep(2000L);

		Tester.testFunction("Pull config from Nacos", () -> {
			assertThat(userProperties.getAge()).isEqualTo(21);
			assertThat(userProperties.getName()).isEqualTo("freeman");
			assertThat(userProperties.getMap().size()).isEqualTo(2);
			assertThat(userProperties.getUsers().size()).isEqualTo(2);
		});

		Tester.testFunction("Dynamic refresh config", () -> {
			// update config
			updateConfig();

			// wait config refresh
			Thread.sleep(2000L);

			assertThat(userProperties.getAge()).isEqualTo(22);
			assertThat(userProperties.getName()).isEqualTo("freeman1123");
			assertThat(userProperties.getMap().size()).isEqualTo(3);
			assertThat(userProperties.getUsers().size()).isEqualTo(2);
		});
	}

	private void updateConfig() throws NacosException {
		nacosConfigManager.getConfigService().publishConfig("nacos-config-refresh.yml", "DEFAULT_GROUP",
				"configdata:\n" +
					"  user:\n" +
					"    age: 22\n" +
					"    name: freeman1123\n" +
					"    map:\n" +
					"      hobbies:\n" +
					"        - art\n" +
					"        - programming\n" +
					"        - movie\n" +
					"      intro: Hello, I'm freeman\n" +
					"      extra: yo~\n" +
					"    users:\n" +
					"      - name: dad\n" +
					"        age: 20\n" +
					"      - name: mom\n" +
					"        age: 18",
				"yaml");
	}

	static class PushConfigConfiguration {

		@Autowired
		private NacosConfigManager nacosConfigManager;

		@EventListener(ApplicationReadyEvent.class)
		@Order(Ordered.HIGHEST_PRECEDENCE)
		public void applicationReadyEventApplicationListener() throws NacosException {
			// push the config before listening the config
			pushConfig2Nacos(nacosConfigManager.getConfigService());
		}

		private static void pushConfig2Nacos(ConfigService configService)
				throws NacosException {
			configService.publishConfig("nacos-config-refresh.yml", "DEFAULT_GROUP",
					"configdata:\n" +
						"  user:\n" +
						"    age: 21\n" +
						"    name: freeman\n" +
						"    map:\n" +
						"      hobbies:\n" +
						"        - art\n" +
						"        - programming\n" +
						"      intro: Hello, I'm freeman\n" +
						"    users:\n" +
						"      - name: dad\n" +
						"        age: 20\n" +
						"      - name: mom\n" +
						"        age: 18",
					"yaml");
		}
	}

}
