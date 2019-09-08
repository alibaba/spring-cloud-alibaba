/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.diagnostics.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * A {@code FailureAnalyzer} that performs analysis of failures caused by a
 * {@code NacosConnectionFailureException}.
 *
 * @author juven.xuxb
 */
public class NacosConnectionFailureAnalyzer
		extends AbstractFailureAnalyzer<NacosConnectionFailureException> {

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure,
			NacosConnectionFailureException cause) {
		return new FailureAnalysis("Application failed to connect to Nacos server",
				"check your Nacos server config", cause);
	}
}
