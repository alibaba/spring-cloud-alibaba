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

package com.alibaba.cloud.dubbo.http.matcher;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.springframework.http.HttpRequest;

/**
 * Composite {@link HttpRequestMatcher} implementation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class CompositeHttpRequestMatcher extends AbstractHttpRequestMatcher {

	private final List<HttpRequestMatcher> matchers = new LinkedList<>();

	public CompositeHttpRequestMatcher(HttpRequestMatcher... matchers) {
		this.matchers.addAll(Arrays.asList(matchers));
	}

	public CompositeHttpRequestMatcher and(HttpRequestMatcher matcher) {
		this.matchers.add(matcher);
		return this;
	}

	@Override
	public boolean match(HttpRequest request) {
		for (HttpRequestMatcher matcher : matchers) {
			if (!matcher.match(request)) {
				return false;
			}
		}
		return true;
	}

	protected List<HttpRequestMatcher> getMatchers() {
		return this.matchers;
	}

	@Override
	protected Collection<?> getContent() {
		List<Object> content = new LinkedList<>();
		for (HttpRequestMatcher matcher : getMatchers()) {
			if (matcher instanceof AbstractHttpRequestMatcher) {
				content.addAll(((AbstractHttpRequestMatcher) matcher).getContent());
			}
		}
		return content;
	}

	@Override
	protected String getToStringInfix() {
		return " && ";
	}

}
