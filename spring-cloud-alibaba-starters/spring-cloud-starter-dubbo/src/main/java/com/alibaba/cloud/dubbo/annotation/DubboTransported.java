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

package com.alibaba.cloud.dubbo.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.ExporterListener;
import org.apache.dubbo.rpc.Filter;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.client.RestTemplate;

import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_RETRIES;

/**
 * {@link DubboTransported @DubboTransported} annotation indicates that the traditional
 * Spring Cloud Service-to-Service call is transported by Dubbo under the hood, there are
 * two main scenarios:
 * <ol>
 * <li>{@link FeignClient @FeignClient} annotated classes:
 * <ul>
 * If {@link DubboTransported @DubboTransported} annotated classes, the invocation of all
 * methods of {@link FeignClient @FeignClient} annotated classes.
 * </ul>
 * <ul>
 * If {@link DubboTransported @DubboTransported} annotated methods of
 * {@link FeignClient @FeignClient} annotated classes.
 * </ul>
 * </li>
 * <li>{@link LoadBalanced @LoadBalanced} {@link RestTemplate} annotated field, method and
 * parameters</li>
 * </ol>
 * <p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FeignClient
 * @see LoadBalanced
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD,
		ElementType.PARAMETER })
@Documented
public @interface DubboTransported {

	/**
	 * The protocol of Dubbo transport whose value could be used the placeholder
	 * "dubbo.transport.protocol".
	 * @return the default protocol is "dubbo"
	 */
	String protocol() default "${dubbo.transport.protocol:dubbo}";

	/**
	 * The cluster of Dubbo transport whose value could be used the placeholder
	 * "dubbo.transport.cluster".
	 * @return the default cluster is "failover"
	 */
	String cluster() default "${dubbo.transport.cluster:failover}";

	/**
	 * Whether to reconnect if connection is lost, if not specify, reconnect is enabled by
	 * default, and the interval for retry connecting is 2000 ms.
	 *
	 * @see Reference#reconnect()
	 * @return reconnect time
	 */
	String reconnect() default "${dubbo.transport.reconnect:2000}";

	/**
	 * Maximum connections service provider can accept, default value is 0 - connection is
	 * shared.
	 *
	 * @see Reference#connections()
	 * @return maximum connections
	 */
	int connections() default 0;

	/**
	 * Service invocation retry times.
	 *
	 * @see Reference#retries()
	 * @return retry times
	 */
	int retries() default DEFAULT_RETRIES;

	/**
	 * Load balance strategy, legal values include: random, roundrobin, leastactive.
	 *
	 * @see Reference#loadbalance()
	 * @return load balance strategy
	 */
	String loadbalance() default "${dubbo.transport.loadbalance:}";

	/**
	 * Maximum active requests allowed, default value is 0.
	 *
	 * @see Reference#actives()
	 * @return maximum active requests
	 */
	int actives() default 0;

	/**
	 * Timeout value for service invocation, default value is 0.
	 *
	 * @see Reference#timeout()
	 * @return timeout for service invocation
	 */
	int timeout() default 0;

	/**
	 * Specify cache implementation for service invocation, legal values include: lru,
	 * threadlocal, jcache.
	 *
	 * @see Reference#cache()
	 * @return specify cache implementation for service invocation
	 */
	String cache() default "${dubbo.transport.cache:}";

	/**
	 * Filters for service invocation.
	 *
	 * @see Filter
	 * @see Reference#filter()
	 * @return filters for service invocation
	 */
	String[] filter() default {};

	/**
	 * Listeners for service exporting and unexporting.
	 *
	 * @see ExporterListener
	 * @see Reference#listener()
	 * @return listener
	 */
	String[] listener() default {};

	/**
	 * Customized parameter key-value pair, for example: {key1, value1, key2, value2}.
	 *
	 * @see Reference#parameters()
	 * @return parameters
	 */
	String[] parameters() default {};

}
