/*
 * Copyright 2012-2020 the original author or authors.
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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestTimeoutExtension
		implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

	@Override
	public void afterAll(ExtensionContext context) throws Exception {

	}

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {

	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		final Class<?> clazz = context.getRequiredTestClass();
		final TestExtend annotation = clazz.getAnnotation(TestExtend.class);
		ScheduledExecutorService singletonThread = Executors
				.newSingleThreadScheduledExecutor();
		while (!singletonThread.awaitTermination(annotation.time(),
				TimeUnit.MILLISECONDS)) {
			singletonThread.shutdown();
		}
	}
}
