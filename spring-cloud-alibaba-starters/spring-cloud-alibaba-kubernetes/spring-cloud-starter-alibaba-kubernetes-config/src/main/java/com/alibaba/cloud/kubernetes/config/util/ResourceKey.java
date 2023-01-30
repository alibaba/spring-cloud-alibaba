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
	private final boolean refreshable;

	public ResourceKey(String type, String name, String namespace, boolean refreshable) {
		this.type = type;
		this.name = name;
		this.namespace = namespace;
		this.refreshable = refreshable;
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

	public boolean refreshable() {
		return refreshable;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		ResourceKey that = (ResourceKey) obj;
		return Objects.equals(this.type, that.type)
				&& Objects.equals(this.name, that.name)
				&& Objects.equals(this.namespace, that.namespace)
				&& this.refreshable == that.refreshable;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, name, namespace, refreshable);
	}

	@Override
	public String toString() {
		return "ResourceKey[" + "type=" + type + ", " + "name=" + name + ", "
				+ "namespace=" + namespace + ", " + "refreshEnabled=" + refreshable + ']';
	}
}
