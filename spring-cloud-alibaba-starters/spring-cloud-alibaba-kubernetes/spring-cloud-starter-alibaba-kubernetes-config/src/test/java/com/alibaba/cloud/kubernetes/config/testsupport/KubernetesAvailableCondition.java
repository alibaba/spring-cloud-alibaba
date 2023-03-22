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

package com.alibaba.cloud.kubernetes.config.testsupport;

import com.alibaba.cloud.kubernetes.commons.KubernetesUtils;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.util.Assert;

/**
 * @author Freeman
 */
public class KubernetesAvailableCondition implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(
			ExtensionContext extensionContext) {
		try {
			Config config = KubernetesUtils.config();
			Assert.notEmpty(config.getContexts(),
					"No contexts found in kubernetes config");
			try (KubernetesClient client = KubernetesUtils.newKubernetesClient()) {
				client.configMaps().inNamespace(KubernetesUtils.currentNamespace())
						.list();
			}
		}
		catch (Throwable e) {
			return ConditionEvaluationResult.disabled("Kubernetes unavailable");
		}
		return ConditionEvaluationResult.enabled("Kubernetes available");
	}
}
