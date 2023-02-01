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

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.DockerClientFactory;

/**
 * An {@link ExecutionCondition} that disables execution if Docker is unavailable.
 *
 * @author freeman
 * @since 2021.0.1.0
 */
class HasDockerAndItEnabledCondition implements ExecutionCondition {

	private static final String RUN_INTEGRATION_TESTS_PROPERTY = "it.enabled";

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult
			.enabled("Docker available.");

	private static final ConditionEvaluationResult DISABLED = ConditionEvaluationResult
			.disabled("Default not run integration tests, you can set '"
					+ RUN_INTEGRATION_TESTS_PROPERTY + "=true' to enable.");

	private static final ConditionEvaluationResult DOCKER_DISABLED = ConditionEvaluationResult
			.disabled("Docker unavailable.");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(
			ExtensionContext context) {
		try {
			if (Boolean
					.parseBoolean(System.getProperty(RUN_INTEGRATION_TESTS_PROPERTY))) {
				DockerClientFactory.instance().client();
				return ENABLED;
			}
			else {
				return DISABLED;
			}
		}
		catch (Throwable ignored) {
			return DOCKER_DISABLED;
		}
	}

}
