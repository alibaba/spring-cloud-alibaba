/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.opensergo.condition;

import com.alibaba.cloud.opensergo.OpenSergoConstants;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * A condition that checks if OpenSergo is configured.
 *
 * @author luyanbo
 */
public class OnEnvCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String bootstrap = context.getEnvironment()
				.getProperty(OpenSergoConstants.OPENSERGO_BOOTSTRAP);
		String bootstrapConfig = context.getEnvironment()
				.getProperty(OpenSergoConstants.OPENSERGO_BOOTSTRAP_CONFIG);
		return StringUtils.hasLength(bootstrap) || StringUtils.hasLength(bootstrapConfig);
	}
}
