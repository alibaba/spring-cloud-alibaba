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

package com.alibaba.cloud.kubernetes.config.util;

import java.util.Objects;

/**
 * @author Freeman
 */
public final class ResourceKey {
	private final String type;
	private final String name;
	private final String namespace;

	public ResourceKey(String type, String name, String namespace) {
		this.type = type;
		this.name = name;
		this.namespace = namespace;
	}

	public String type() {
		return type;
	}

	public String name() {
		return name;
	}

	public String namespace() {
		return namespace;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResourceKey that = (ResourceKey) o;
		return Objects.equals(type, that.type) && Objects.equals(name, that.name)
				&& Objects.equals(namespace, that.namespace);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, namespace);
	}

	@Override
	public String toString() {
		return "ResourceKey{" + "type='" + type + '\'' + ", name='" + name + '\''
				+ ", namespace='" + namespace + '\'' + '}';
	}
}
