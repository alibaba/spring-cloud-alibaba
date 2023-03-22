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

package com.alibaba.cloud.kubernetes.config.it;

import com.alibaba.cloud.kubernetes.config.testsupport.KubernetesAvailable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static com.alibaba.cloud.kubernetes.config.testsupport.KubernetesTestUtil.createOrReplaceConfigMap;
import static com.alibaba.cloud.kubernetes.config.testsupport.KubernetesTestUtil.createOrReplaceSecret;
import static com.alibaba.cloud.kubernetes.config.testsupport.KubernetesTestUtil.deleteConfigMap;
import static com.alibaba.cloud.kubernetes.config.testsupport.KubernetesTestUtil.deleteSecret;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author Freeman
 */
@KubernetesAvailable
@SpringBootTest(classes = Empty.class, webEnvironment = NONE)
@ActiveProfiles("secret")
public class SecretIntegrationTests {

	@BeforeAll
	static void init() {
		createOrReplaceConfigMap("secret/configmap.yaml");
		createOrReplaceSecret("secret/secret.yaml");
		createOrReplaceSecret("secret/secret-refreshable.yaml");
	}

	@AfterAll
	static void recover() {
		deleteConfigMap("secret/configmap.yaml");
		deleteSecret("secret/secret.yaml");
		deleteSecret("secret/secret-refreshable.yaml");
	}

	@Autowired
	private Environment env;

	@Test
	void testSecret() throws InterruptedException {
		// secret win, configmap lose
		assertThat(env.getProperty("username")).isNotEqualTo("admin");
		assertThat(env.getProperty("username")).isNotEqualTo("cm9vdAo=");
		assertThat(env.getProperty("username")).isEqualTo("root");
		assertThat(env.getProperty("password")).isNotEqualTo("666");
		assertThat(env.getProperty("password")).isNotEqualTo("MTEyMzIyMwo=");
		assertThat(env.getProperty("password")).isEqualTo("1123223"); // MTEyMzIyMwo=

		assertThat(env.getProperty("price")).isEqualTo("100");

		assertThat(env.getProperty("hobbies[0]")).isEqualTo("reading");
		assertThat(env.getProperty("hobbies[1]")).isEqualTo("writing");
		assertThat(env.getProperty("hobbies[2]")).isNull();

		createOrReplaceSecret("secret/secret-changed.yaml");
		createOrReplaceSecret("secret/secret-refreshable-changed.yaml");

		// make sure context is refreshed
		Thread.sleep(1000);

		// secret refresh is disabled by default
		assertThat(env.getProperty("username")).isNotEqualTo("root2");
		assertThat(env.getProperty("username")).isEqualTo("root");

		// test refreshable secret
		assertThat(env.getProperty("price")).isEqualTo("200");
	}
}
