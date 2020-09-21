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

package com.alibaba.cloud.dubbo.service.parameter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.alibaba.cloud.dubbo.http.HttpServerRequest;
import com.alibaba.cloud.dubbo.metadata.MethodParameterMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;

import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Abstract HTTP Names Value {@link DubboGenericServiceParameterResolver Dubbo
 * GenericService Parameter Resolver}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractNamedValueServiceParameterResolver
		extends AbstractDubboGenericServiceParameterResolver {

	/**
	 * Get the {@link MultiValueMap} of names and values.
	 * @param request Http server request
	 * @return map of name and values
	 */
	protected abstract MultiValueMap<String, String> getNameAndValuesMap(
			HttpServerRequest request);

	@Override
	public Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata, HttpServerRequest request) {

		Collection<String> names = getNames(restMethodMetadata, methodParameterMetadata);

		if (isEmpty(names)) { // index can't match
			return null;
		}

		MultiValueMap<String, String> nameAndValues = getNameAndValuesMap(request);

		String targetName = null;

		for (String name : names) {
			if (nameAndValues.containsKey(name)) {
				targetName = name;
				break;
			}
		}

		if (targetName == null) { // request parameter is abstract
			return null;
		}

		Class<?> parameterType = resolveClass(methodParameterMetadata.getType());

		Object paramValue = null;

		if (parameterType.isArray()) { // Array type
			paramValue = nameAndValues.get(targetName);
		}
		else {
			paramValue = nameAndValues.getFirst(targetName);
		}

		return resolveValue(paramValue, parameterType);
	}

	@Override
	public Object resolve(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata,
			RestMethodMetadata clientRestMethodMetadata, Object[] arguments) {

		Collection<String> names = getNames(restMethodMetadata, methodParameterMetadata);

		if (isEmpty(names)) { // index can't match
			return null;
		}

		Integer index = null;

		Map<Integer, Collection<String>> clientIndexToName = clientRestMethodMetadata
				.getIndexToName();

		for (Map.Entry<Integer, Collection<String>> entry : clientIndexToName
				.entrySet()) {

			Collection<String> clientParamNames = entry.getValue();

			if (CollectionUtils.containsAny(names, clientParamNames)) {
				index = entry.getKey();
				break;
			}
		}

		return index > -1 ? arguments[index] : null;
	}

	protected Collection<String> getNames(RestMethodMetadata restMethodMetadata,
			MethodParameterMetadata methodParameterMetadata) {

		Map<Integer, Collection<String>> indexToName = restMethodMetadata
				.getIndexToName();

		int index = methodParameterMetadata.getIndex();

		Collection<String> paramNames = indexToName.get(index);

		return paramNames == null ? Collections.emptyList() : paramNames;
	}

}
