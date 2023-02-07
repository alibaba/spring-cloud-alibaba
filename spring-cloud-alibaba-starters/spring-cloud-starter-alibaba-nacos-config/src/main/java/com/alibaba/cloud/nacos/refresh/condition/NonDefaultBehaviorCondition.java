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

package com.alibaba.cloud.nacos.refresh.condition;

import com.alibaba.cloud.nacos.refresh.RefreshBehavior;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.alibaba.cloud.nacos.refresh.RefreshBehavior.ALL_BEANS;

/**
 * @author freeman
 * @since 2021.0.1.1
 */
public class NonDefaultBehaviorCondition extends SpringBootCondition {

	private static final RefreshBehavior DEFAULT_REFRESH_BEHAVIOR = ALL_BEANS;

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		RefreshBehavior behavior = context.getEnvironment().getProperty(
				"spring.cloud.nacos.config.refresh-behavior", RefreshBehavior.class,
				DEFAULT_REFRESH_BEHAVIOR);
		if (DEFAULT_REFRESH_BEHAVIOR == behavior) {
			return ConditionOutcome.noMatch("no matched");
		}
		return ConditionOutcome.match("matched");
	}

}
