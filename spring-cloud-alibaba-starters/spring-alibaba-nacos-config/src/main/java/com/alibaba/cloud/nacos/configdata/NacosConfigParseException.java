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

package com.alibaba.cloud.nacos.configdata;

import org.springframework.boot.context.config.ConfigDataException;

/**
 * Thrown when a Nacos config has been fetched from the server successfully but its
 * content cannot be parsed, for example because of duplicate YAML keys.
 *
 * <p>
 * This is intentionally distinct from
 * {@link org.springframework.boot.context.config.ConfigDataResourceNotFoundException}.
 * The config resource does exist and was retrieved, so reporting it as "not found"
 * would mislead users into prefixing the import with {@code optional:} and silently
 * skipping a genuinely broken config. Surfacing the real parsing error keeps the root
 * cause actionable.
 *
 * <p>
 * It extends {@link ConfigDataException} to sit in the same family as the other
 * config-data failures, but deliberately not {@code ConfigDataNotFoundException}, so an
 * unparseable config is never silently skipped by the {@code optional:} handling.
 *
 * @since 2025.1.0.1
 */
public class NacosConfigParseException extends ConfigDataException {

	private final String dataId;

	private final String group;

	public NacosConfigParseException(String dataId, String group, Throwable cause) {
		super(buildMessage(dataId, group, cause), cause);
		this.dataId = dataId;
		this.group = group;
	}

	private static String buildMessage(String dataId, String group, Throwable cause) {
		String reason = (cause == null) ? "unknown cause" : cause.toString();
		return String.format(
				"Failed to parse Nacos config content[dataId=%s, group=%s]: %s. "
						+ "The config was fetched successfully, so this is a content problem "
						+ "(for example duplicate keys or malformed syntax) rather than a missing config.",
				dataId, group, reason);
	}

	public String getDataId() {
		return this.dataId;
	}

	public String getGroup() {
		return this.group;
	}

}
