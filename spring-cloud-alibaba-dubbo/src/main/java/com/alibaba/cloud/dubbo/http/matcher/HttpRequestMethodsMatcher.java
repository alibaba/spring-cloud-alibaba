/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.http.matcher;

import static org.springframework.http.HttpMethod.resolve;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.util.StringUtils;

/**
 * {@link HttpRequest} {@link HttpMethod methods} {@link HttpRequestMatcher matcher}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpRequestMethodsMatcher extends AbstractHttpRequestMatcher {

	private final Set<HttpMethod> methods;

	public HttpRequestMethodsMatcher(String... methods) {
		this.methods = resolveHttpMethods(methods);
	}

	private Set<HttpMethod> resolveHttpMethods(String[] methods) {
		Set<HttpMethod> httpMethods = new LinkedHashSet<>(methods.length);
		for (String method : methods) {
			if (!StringUtils.hasText(method)) {
				continue;
			}
			HttpMethod httpMethod = resolve(method);
			httpMethods.add(httpMethod);
		}
		return httpMethods;
	}

	public Set<HttpMethod> getMethods() {
		return methods;
	}

	@Override
	public boolean match(HttpRequest request) {
		boolean matched = false;
		HttpMethod httpMethod = request.getMethod();
		if (httpMethod != null) {
			for (HttpMethod method : getMethods()) {
				if (httpMethod.equals(method)) {
					matched = true;
					break;
				}
			}
		}
		return matched;
	}

	@Override
	protected Collection<HttpMethod> getContent() {
		return methods;
	}

	@Override
	protected String getToStringInfix() {
		return " || ";
	}
}
