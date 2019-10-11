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

package com.alibaba.cloud.nacos.parser;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * @author element
 */
public class OrderedProperties extends Properties {

	private final LinkedHashSet<Object> keys;

	public OrderedProperties() {
		this.keys = new LinkedHashSet();
	}

	@Override
	public Enumeration<Object> keys() {
		return Collections.enumeration(this.keys);
	}

	@Override
	public Object put(Object key, Object value) {
		this.keys.add(key);
		return super.put(key, value);
	}

	@Override
	public Set<Object> keySet() {
		return this.keys;
	}

	@Override
	public Set<String> stringPropertyNames() {
		Set<String> set = new LinkedHashSet();
		Iterator iterator = this.keys.iterator();

		while (iterator.hasNext()) {
			Object key = iterator.next();
			set.add((String) key);
		}

		return set;
	}

	@Override
	public Enumeration<String> propertyNames() {
		Vector<String> vector = new Vector<>();
		Iterator iterator = this.keys.iterator();

		while (iterator.hasNext()) {
			Object key = iterator.next();
			vector.add((String) key);
		}
		return vector.elements();
	}

}
