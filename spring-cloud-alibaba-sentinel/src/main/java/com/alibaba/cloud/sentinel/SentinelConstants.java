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

package com.alibaba.cloud.sentinel;

/**
 * @author fangjian
 */
public interface SentinelConstants {

	String PROPERTY_PREFIX = "spring.cloud.sentinel";

	String BLOCK_TYPE = "block";
	String FALLBACK_TYPE = "fallback";
	String URLCLEANER_TYPE = "urlCleaner";

	// commercialization

	String FLOW_DATASOURCE_NAME = "edas-flow";
	String DEGRADE_DATASOURCE_NAME = "edas-degrade";

}
