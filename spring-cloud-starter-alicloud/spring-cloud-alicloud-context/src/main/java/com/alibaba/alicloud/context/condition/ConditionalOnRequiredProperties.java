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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * The annotation to combine the multiple {@link ConditionalOnRequiredProperty}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2.2.1
 * @see ConditionalOnRequiredProperty
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
@Conditional(OnRequiredPropertiesCondition.class)
public @interface ConditionalOnRequiredProperties {

	/**
	 * @return the multiple {@link ConditionalOnRequiredProperty}
	 */
	ConditionalOnRequiredProperty[] value();
}
