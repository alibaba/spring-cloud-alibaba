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

package com.alibaba.cloud.testsupport;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@Slf4j
final class SpringCloudAlibabaExtension
		implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
	private final boolean LOCAL_MODE = Objects.equals(System.getProperty("local"),
			"true");

	private DockerComposeContainer<?> compose;

	@Override
	@SuppressWarnings("UnstableApiUsage")
	public void beforeAll(ExtensionContext context) throws IOException {
		Awaitility.setDefaultTimeout(Duration.ofSeconds(60));
		Awaitility.setDefaultPollInterval(Duration.ofSeconds(10));

		if (LOCAL_MODE) {
			runInLocal();
		}
		else {
			runInDockerContainer(context);
		}
	}

	private void runInLocal() {
		Testcontainers.exposeHostPorts(3000);
	}

	private void runInDockerContainer(ExtensionContext context) {
		compose = createDockerCompose(context);
		compose.start();
	}

	@Override
	public void afterAll(ExtensionContext context) {
		if (compose != null) {
			compose.stop();
		}
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		final Object instance = context.getRequiredTestInstance();
		Stream.of(instance.getClass().getDeclaredFields());
	}

	private DockerComposeContainer<?> createDockerCompose(ExtensionContext context) {
		final Class<?> clazz = context.getRequiredTestClass();
		final SpringCloudAlibaba annotation = clazz
				.getAnnotation(SpringCloudAlibaba.class);
		final List<File> files = Stream.of(annotation.composeFiles())
				.map(it -> SpringCloudAlibaba.class.getClassLoader().getResource(it))
				.filter(Objects::nonNull).map(URL::getPath).map(File::new)
				.collect(Collectors.toList());
		compose = new DockerComposeContainer<>(files).withPull(true)
				.withTailChildContainers(true);

		return compose;
	}
}
