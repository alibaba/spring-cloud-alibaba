/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.alicloud.context.condition;

import com.alibaba.alicloud.context.AliCloudProperties;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.alibaba.alicloud.context.AliCloudProperties.ACCESS_KEY_PROPERTY;
import static com.alibaba.alicloud.context.AliCloudProperties.SECRET_KEY_PROPERTY;
import static org.springframework.boot.autoconfigure.condition.ConditionOutcome.noMatch;

/**
 * {@link Condition} that checks whether an endpoint of Alibaba Cloud is available or not
 * if and only if the properties that are "spring.cloud.alicloud.access-key" and
 * "spring.cloud.alicloud.secret-key" must be present.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 * @see ConditionalOnAliCloudEndpoint
 * @see AliCloudProperties
 */
class OnAliCloudEndpointCondition extends SpringBootCondition {

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		Environment environment = context.getEnvironment();
		return (environment.containsProperty(ACCESS_KEY_PROPERTY)
				&& environment.containsProperty(SECRET_KEY_PROPERTY))
						? ConditionOutcome.match()
						: noMatch("The properties '" + ACCESS_KEY_PROPERTY + "' and '"
								+ SECRET_KEY_PROPERTY
								+ "' must be present at the same time.");
	}
}
