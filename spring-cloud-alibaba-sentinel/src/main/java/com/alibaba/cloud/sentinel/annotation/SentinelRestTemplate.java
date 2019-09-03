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

package com.alibaba.cloud.sentinel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author fangjian
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SentinelRestTemplate {

	/** method of flow,it must be static and have this parameters
	 * ,the argument returned must be SentinelClientHttpResponse
	 * e.g :
	 * public static SentinelClientHttpResponse block(
	 * 		HttpRequest request, byte[] body, ClientHttpRequestExecution execution, BlockException ex)
	 */
	String blockHandler() default "";

	/** class of flow , it must be public */
	Class<?> blockHandlerClass() default void.class;

	/** method of degrade,it must be static and have this parameters
	 * ,the argument returned must be SentinelClientHttpResponse
	 * e.g :
	 * public static SentinelClientHttpResponse degrade(
	 * 		HttpRequest request, byte[] body, ClientHttpRequestExecution execution, BlockException ex)
	 */
	String fallback() default "";

	/** class of degrade , it must be public */
	Class<?> fallbackClass() default void.class;

	String urlCleaner() default "";

	Class<?> urlCleanerClass() default void.class;

	/** the mock result is used by default when degradation occurs */
	boolean mockEnabled() default true;
}
