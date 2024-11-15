/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.configdata;

/**
 * Config preference.
 * <p>
 * When configured profile specific configuration, local config will override the remote,
 * because the local config is <strong>profile specific</strong>, it has higher priority.
 * <p>
 * So give remote config a chance to "win", we treat remote config as profile specific, it
 * should be included after profile specific sibling imports. Eventually, it will override
 * the local profile specific config.
 *
 * @author freeman
 * @since 2021.0.4.0
 */
public enum ConfigPreference {
	/**
	 * Prefer local configuration.
	 */
	LOCAL,
	/**
	 * Prefer remote configuration.
	 */
	REMOTE
}
