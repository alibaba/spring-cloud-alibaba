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

package com.alibaba.cloud.dubbo.util;

/**
 * The constants for Dubbo Spring Cloud.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public final class DubboCloudConstants {

	/**
	 * The property prefix of Configuration.
	 */
	public static final String CONFIG_PROPERTY_PREFIX = "dubbo.cloud";

	/**
	 * The property name of Registry type.
	 */
	public static final String REGISTRY_TYPE_PROPERTY_NAME = CONFIG_PROPERTY_PREFIX
			+ ".registry-type";

	/**
	 * The property value of Spring Cloud Registry.
	 */
	public static final String SPRING_CLOUD_REGISTRY_PROPERTY_VALUE = "spring-cloud";

	/**
	 * The property value of Dubbo Cloud Registry.
	 */
	public static final String DUBBO_CLOUD_REGISTRY_PROPERTY_VALUE = "dubbo-cloud";

	private DubboCloudConstants() {
		throw new AssertionError("Must not instantiate constant utility class");
	}

}
