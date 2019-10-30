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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;

/**
 * {@link HttpRequest} headers {@link HttpRequestMatcher matcher}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpRequestHeadersMatcher extends AbstractHttpRequestMatcher {

	private final Set<HeaderExpression> expressions;

	public HttpRequestHeadersMatcher(String... headers) {
		this.expressions = parseExpressions(headers);
	}

	private static Set<HeaderExpression> parseExpressions(String... headers) {
		Set<HeaderExpression> expressions = new LinkedHashSet<>();
		for (String header : headers) {
			HeaderExpression expr = new HeaderExpression(header);
			if (HttpHeaders.ACCEPT.equalsIgnoreCase(expr.name)
					|| HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(expr.name)) {
				continue;
			}
			expressions.add(expr);
		}
		return expressions;
	}

	@Override
	public boolean match(HttpRequest request) {
		for (HeaderExpression expression : this.expressions) {
			if (!expression.match(request)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected Collection<HeaderExpression> getContent() {
		return this.expressions;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}
}
