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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.util.ObjectUtils;

/**
 * Parses and matches a single header expression to a request.
 * <p>
 * The some source code is scratched from
 * org.springframework.web.servlet.mvc.condition.HeadersRequestCondition.HeaderExpression
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
class HeaderExpression extends AbstractNameValueExpression<String> {

	HeaderExpression(String expression) {
		super(expression);
	}

	@Override
	protected boolean isCaseSensitiveName() {
		return false;
	}

	@Override
	protected String parseValue(String valueExpression) {
		return valueExpression;
	}

	@Override
	protected boolean matchName(HttpRequest request) {
		HttpHeaders httpHeaders = request.getHeaders();
		return httpHeaders.containsKey(this.name);
	}

	@Override
	protected boolean matchValue(HttpRequest request) {
		HttpHeaders httpHeaders = request.getHeaders();
		String headerValue = httpHeaders.getFirst(this.name);
		return ObjectUtils.nullSafeEquals(this.value, headerValue);
	}
}
