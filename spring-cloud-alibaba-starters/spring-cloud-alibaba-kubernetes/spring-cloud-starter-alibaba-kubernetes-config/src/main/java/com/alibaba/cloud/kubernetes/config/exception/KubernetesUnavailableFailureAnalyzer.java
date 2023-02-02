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

import java.util.stream.Collectors;

import com.alibaba.cloud.kubernetes.commons.KubernetesUtils;
import io.fabric8.kubernetes.api.model.NamedContext;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Freeman
 */
public class KubernetesUnavailableFailureAnalyzer
		extends AbstractFailureAnalyzer<KubernetesUnavailableException> {
	@Override
	protected FailureAnalysis analyze(Throwable rootFailure,
			KubernetesUnavailableException cause) {
		String description = String.format(
				"Current context '%s' can not connect to Kubernetes cluster.\n\n"
						+ "Are you sure you've set the right context? Available contexts are: %s.\n",
				KubernetesUtils.config().getCurrentContext().getName(),
				KubernetesUtils.config().getContexts().stream().map(NamedContext::getName)
						.collect(Collectors.toList()));
		String action = "Please check your kube config file and Kubernetes cluster status.";
		return new FailureAnalysis(description, action, cause);
	}
}
