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

/**
 * @author Freeman
 */
public final class Pair<K, V> {
	private final K key;
	private final V right;

	private Pair(K key, V right) {
		this.key = key;
		this.right = right;
	}

	public K key() {
		return key;
	}

	public V value() {
		return right;
	}

	public static <K, V> Pair<K, V> of(K key, V value) {
		return new Pair<>(key, value);
	}
}
