/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.commons.governance.routing.rule;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public interface Rule {

	/**
	 * get type of rule.
	 * @return String
	 */
	default String getType() {
		return null;
	}

	/**
	 * get condition.
	 * @return String
	 */
	default String getCondition() {
		return null;
	}

	/**
	 * set condition.
	 * @param condition {@link String}
	 */
	default void setCondition(String condition) {
	}

	/**
	 * get key of rule.
	 * @return String
	 */
	default String getKey() {
		return null;
	}

	/**
	 * set key of rule.
	 * @param key {@link String}
	 */
	default void setKey(String key) {
	}

	/**
	 * get value of rule.
	 * @return String
	 */
	default String getValue() {
		return null;
	}

	/**
	 * set value of rule.
	 * @param value {@link String}
	 */
	default void setValue(String value) {
	}

}
