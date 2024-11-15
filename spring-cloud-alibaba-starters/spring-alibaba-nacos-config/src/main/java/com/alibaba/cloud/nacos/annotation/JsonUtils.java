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

package com.alibaba.cloud.nacos.annotation;

import java.io.IOException;
import java.lang.reflect.Type;

import com.alibaba.nacos.api.exception.runtime.NacosDeserializationException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

final class JsonUtils {

	private JsonUtils() {
	}

	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	}

	/**
	 * Json string deserialize to Object.
	 *
	 * @param json json string
	 * @param cls  class of object
	 * @param <T>  General type
	 * @return object
	 * @throws NacosDeserializationException if deserialize failed
	 */
	public static <T> T toObj(String json, Class<T> cls) {
		try {
			return mapper.readValue(json, cls);
		}
		catch (IOException e) {
			throw new NacosDeserializationException(cls, e);
		}
	}

	public static <T> T toObj(String json, Type type) {
		try {
			return mapper.readValue(json, TypeFactory.defaultInstance().constructType(type));
		}
		catch (IOException e) {
			throw new NacosDeserializationException(type, e);
		}
	}
}
