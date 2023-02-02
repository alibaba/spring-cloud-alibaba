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

import com.alibaba.cloud.kubernetes.commons.KubernetesUtils;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Freeman
 */
public class KubernetesForbiddenFailureAnalyzer
		extends AbstractFailureAnalyzer<KubernetesForbiddenException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure,
			KubernetesForbiddenException cause) {
		String description = String.format(
				"It looks like you don't have enough access to the resource (%s '%s.%s') in context '%s'.",
				cause.getType().getSimpleName(), cause.getName(), cause.getNamespace(),
				KubernetesUtils.config().getCurrentContext().getName());
		String action = "Please ask the administrator to add enough permissions for you, or are you just in the wrong context?";
		return new FailureAnalysis(description, action, cause);
	}
}
