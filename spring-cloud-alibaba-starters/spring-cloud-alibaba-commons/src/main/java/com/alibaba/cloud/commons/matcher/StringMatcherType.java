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

package com.alibaba.cloud.commons.matcher;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public enum StringMatcherType {

	/**
	 * exact match.
	 */
	EXACT("exact"),
	/**
	 * prefix match.
	 */
	PREFIX("prefix"),
	/**
	 * suffix match.
	 */
	SUFFIX("suffix"),
	/**
	 * present match.
	 */
	PRESENT("present"),
	/**
	 * regex match.
	 */
	REGEX("regex"),
	/**
	 * contain match.
	 */
	CONTAIN("contain");

	/**
	 * type of matcher.
	 */
	public final String type;

	StringMatcherType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.type;
	}

}
