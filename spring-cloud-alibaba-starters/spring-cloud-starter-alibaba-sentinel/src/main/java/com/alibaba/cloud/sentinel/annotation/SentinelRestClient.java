/*
 * Copyright 2013-present the original author or authors.
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

package com.alibaba.cloud.sentinel.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for Sentinel integration with Spring's RestClient.Builder.
 *
 * <p>
 * This annotation should be placed on a {@code @Bean} method that returns
 * {@link org.springframework.web.client.RestClient.Builder}. The Sentinel
 * interceptor will be automatically added to the builder.
 *
 * <p>
 * Example usage:
 * <pre>
 * &#64;Bean
 * &#64;SentinelRestClient(blockHandler = "handleBlock",
 *     blockHandlerClass = MyBlockHandler.class)
 * public RestClient.Builder restClientBuilder() {
 *     return RestClient.builder();
 * }
 * </pre>
 *
 * <p>
 * The blockHandler method signature should be:
 * <pre>
 * public static ClientHttpResponse handleBlock(HttpRequest request,
 *     byte[] body, ClientHttpRequestExecution execution, BlockException ex)
 * </pre>
 *
 * <p>
 * The fallback method signature should be:
 * <pre>
 * public static ClientHttpResponse handleFallback(HttpRequest request,
 *     byte[] body, ClientHttpRequestExecution execution, BlockException ex)
 * </pre>
 *
 * <p>
 * The urlCleaner method signature should be:
 * <pre>
 * public static String cleanUrl(String url)
 * </pre>
 *
 * @author QHT, uuuyuqi
 * @see SentinelRestTemplate
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SentinelRestClient {

	/**
	 * Name of the block handler method.
	 * @return name of the block handler method
	 */
	String blockHandler() default "";

	/**
	 * Class containing the block handler method. Must be a static method.
	 * @return class containing the block handler method
	 */
	Class<?> blockHandlerClass() default void.class;

	/**
	 * Name of the fallback method (for circuit breaking).
	 * @return name of the fallback method
	 */
	String fallback() default "";

	/**
	 * Class containing the fallback method. Must be a static method.
	 * @return class containing the fallback method
	 */
	Class<?> fallbackClass() default void.class;

	/**
	 * Name of the URL cleaner method.
	 * @return name of the URL cleaner method
	 */
	String urlCleaner() default "";

	/**
	 * Class containing the URL cleaner method. Must be a static method.
	 * @return class containing the URL cleaner method
	 */
	Class<?> urlCleanerClass() default void.class;

}
