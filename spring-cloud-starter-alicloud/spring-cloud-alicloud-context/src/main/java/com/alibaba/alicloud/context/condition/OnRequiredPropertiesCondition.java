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

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.alibaba.alicloud.context.condition.OnRequiredPropertyCondition.doGetMatchOutcome;

/**
 * Multiple {@link OnRequiredPropertyCondition}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 */
public class OnRequiredPropertiesCondition extends SpringBootCondition {

	private static final String annotationName = ConditionalOnRequiredProperties.class
			.getName();

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {

		Map<String, Object> annotationAttributes = metadata
				.getAnnotationAttributes(annotationName);

		AnnotationAttributes[] value = (AnnotationAttributes[]) annotationAttributes
				.get("value");

		ConditionOutcome result = null;

		for (AnnotationAttributes attributes : value) {
			result = doGetMatchOutcome(context, attributes);
			if (!result.isMatch()) {
				break;
			}
		}
		return result;
	}
}
