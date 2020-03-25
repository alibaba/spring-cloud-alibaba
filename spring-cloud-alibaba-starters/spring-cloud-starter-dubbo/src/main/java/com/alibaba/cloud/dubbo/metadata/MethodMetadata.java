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

package com.alibaba.cloud.dubbo.metadata;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link Method} Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MethodMetadata {

	private String name;

	@JsonProperty("return-type")
	private String returnType;

	private List<MethodParameterMetadata> params;

	@JsonIgnore
	private Method method;

	public MethodMetadata() {
		this.params = new LinkedList<>();
	}

	public MethodMetadata(Method method) {
		this.name = method.getName();
		this.returnType = method.getReturnType().getName();
		this.params = initParameters(method);
		this.method = method;
	}

	private List<MethodParameterMetadata> initParameters(Method method) {
		int parameterCount = method.getParameterCount();
		if (parameterCount < 1) {
			return Collections.emptyList();
		}
		List<MethodParameterMetadata> params = new ArrayList<>(parameterCount);
		Parameter[] parameters = method.getParameters();
		for (int i = 0; i < parameterCount; i++) {
			Parameter parameter = parameters[i];
			MethodParameterMetadata param = toMethodParameterMetadata(i, parameter);
			params.add(param);
		}
		return params;
	}

	private MethodParameterMetadata toMethodParameterMetadata(int index,
			Parameter parameter) {
		MethodParameterMetadata metadata = new MethodParameterMetadata();
		metadata.setIndex(index);
		metadata.setName(parameter.getName());
		metadata.setType(parameter.getType().getTypeName());
		return metadata;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public List<MethodParameterMetadata> getParams() {
		return params;
	}

	public void setParams(List<MethodParameterMetadata> params) {
		this.params = params;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MethodMetadata that = (MethodMetadata) o;
		return Objects.equals(name, that.name)
				&& Objects.equals(returnType, that.returnType)
				&& Objects.equals(params, that.params);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, returnType, params);
	}

	@Override
	public String toString() {
		return "MethodMetadata{" + "name='" + name + '\'' + ", returnType='" + returnType
				+ '\'' + ", params=" + params + ", method=" + method + '}';
	}

}
