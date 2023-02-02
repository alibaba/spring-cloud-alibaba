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

package com.alibaba.cloud.kubernetes.config.exception;

import com.alibaba.cloud.kubernetes.config.KubernetesConfigProperties;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Freeman
 */
public class KubernetesConfigMissingFailureAnalyzer
		extends AbstractFailureAnalyzer<KubernetesConfigMissingException> {
	@Override
	protected FailureAnalysis analyze(Throwable rootFailure,
			KubernetesConfigMissingException cause) {
		String description = String.format("%s name '%s' is missing in namespace '%s'",
				cause.getType().getSimpleName(), cause.getName(), cause.getNamespace());
		String action = String.format(
				"You can set '%s.fail-on-missing-config' to 'false' to not prevent the application start up.",
				KubernetesConfigProperties.PREFIX);
		return new FailureAnalysis(description, action, cause);
	}
}
