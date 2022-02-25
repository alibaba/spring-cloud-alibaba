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

package com.alibaba.cloud.imports.examples;

import java.io.IOException;

import com.alibaba.cloud.imports.examples.model.UserConfig;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.testsupport.HasDockerAndItEnabled;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;

import static com.alibaba.cloud.imports.examples.ConfigRefreshTest.PushConfigConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * Nacos dynamic refresh config function test.
 *
 * @author freeman
 */
@HasDockerAndItEnabled
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(PushConfigConfiguration.class)
public class ConfigRefreshTest {

	@LocalServerPort
	private int port;

	private static final GenericContainer nacos = new GenericContainer("freemanlau/nacos:1.4.2")
			.withExposedPorts(8848)
			.withEnv("MODE", "standalone")
			.withEnv("JVM_XMS", "256m")
			.withEnv("JVM_XMX", "256m")
			.withEnv("JVM_XMN", "128m");

	private static final String serverAddr;

	static {
		try {
			nacos.start();
			// make sure nacos server is ready !
			Thread.sleep(2000L);
		}
		catch (InterruptedException ignored) {
		}

		serverAddr = "127.0.0.1:" + nacos.getMappedPort(8848);
		System.setProperty("spring.cloud.nacos.config.server-addr", serverAddr);
	}

	@Autowired
	private TestRestTemplate restTemplate;
	@Autowired
	private NacosConfigManager nacosConfigManager;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testRefreshConfig() throws IOException, NacosException, InterruptedException {

		ResponseEntity<String> response = restTemplate.getForEntity("http://127.0.0.1:" + port, String.class);
		UserConfig userConfig = objectMapper.readValue(response.getBody(), UserConfig.class);

		System.out.println("====== before refresh ======");
		System.out.println(objectMapper.writeValueAsString(userConfig));
		assertThat(userConfig.getAge()).isEqualTo(21);
		assertThat(userConfig.getName()).isEqualTo("freeman");
		assertThat(userConfig.getMap().size()).isEqualTo(2);
		assertThat(userConfig.getUsers().size()).isEqualTo(2);

		// update config
		updateConfig();

		// wait config refresh
		Thread.sleep(2000L);

		response = restTemplate.getForEntity("http://127.0.0.1:" + port, String.class);
		userConfig = objectMapper.readValue(response.getBody(), UserConfig.class);

		System.out.println("====== after refresh ======");
		System.out.println(objectMapper.writeValueAsString(userConfig));
		assertThat(userConfig.getAge()).isEqualTo(22);
		assertThat(userConfig.getName()).isEqualTo("freeman1123");
		assertThat(userConfig.getMap().size()).isEqualTo(3);
		assertThat(userConfig.getUsers().size()).isEqualTo(2);
	}

	private void updateConfig() throws NacosException {
		nacosConfigManager.getConfigService().publishConfig("test.yml", "DEFAULT_GROUP",
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
				"        age: 18", "yaml");
	}


	static class PushConfigConfiguration {

		@Autowired
		private NacosConfigManager nacosConfigManager;

		@EventListener(ApplicationReadyEvent.class)
		@Order(Ordered.HIGHEST_PRECEDENCE)
		public void applicationReadyEventApplicationListener() throws NacosException {
			pushConfig2Nacos(nacosConfigManager.getConfigService());
		}

		private static void pushConfig2Nacos(ConfigService configService)
				throws NacosException {
			configService.publishConfig("test.yml", "DEFAULT_GROUP",
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
							"        age: 18", "yaml");
		}
	}

}
