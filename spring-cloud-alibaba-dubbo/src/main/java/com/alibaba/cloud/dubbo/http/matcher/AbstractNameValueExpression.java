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

import static org.springframework.util.StringUtils.trimWhitespace;

import org.springframework.http.HttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * The some source code is scratched from
 * org.springframework.web.servlet.mvc.condition.AbstractNameValueExpression
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
abstract class AbstractNameValueExpression<T> implements NameValueExpression<T> {

	protected final String name;

	protected final T value;

	protected final boolean negated;

	AbstractNameValueExpression(String expression) {
		int separator = expression.indexOf('=');
		if (separator == -1) {
			this.negated = expression.startsWith("!");
			this.name = trimWhitespace(
					(this.negated ? expression.substring(1) : expression));
			this.value = null;
		}
		else {
			this.negated = (separator > 0) && (expression.charAt(separator - 1) == '!');
			this.name = trimWhitespace(
					(this.negated ? expression.substring(0, separator - 1)
							: expression.substring(0, separator)));
			String valueExpression = getValueExpression(expression, separator);
			this.value = isExcludedValue(valueExpression) ? null
					: parseValue(valueExpression);
		}
	}

	private String getValueExpression(String expression, int separator) {
		return trimWhitespace(expression.substring(separator + 1));
	}

	/**
	 * Exclude the pattern value Expression: "{value}", subclass could override this
	 * method.
	 *
	 * @param valueExpression
	 * @return
	 */
	protected boolean isExcludedValue(String valueExpression) {
		return StringUtils.hasText(valueExpression) && valueExpression.startsWith("{")
				&& valueExpression.endsWith("}");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public T getValue() {
		return this.value;
	}

	@Override
	public boolean isNegated() {
		return this.negated;
	}

	public final boolean match(HttpRequest request) {
		boolean isMatch;
		if (this.value != null) {
			isMatch = matchValue(request);
		}
		else {
			isMatch = matchName(request);
		}
		return (this.negated ? !isMatch : isMatch);
	}

	protected abstract boolean isCaseSensitiveName();

	protected abstract T parseValue(String valueExpression);

	protected abstract boolean matchName(HttpRequest request);

	protected abstract boolean matchValue(HttpRequest request);

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		AbstractNameValueExpression<?> that = (AbstractNameValueExpression<?>) other;
		return ((isCaseSensitiveName() ? this.name.equals(that.name)
				: this.name.equalsIgnoreCase(that.name))
				&& ObjectUtils.nullSafeEquals(this.value, that.value)
				&& this.negated == that.negated);
	}

	@Override
	public int hashCode() {
		int result = (isCaseSensitiveName() ? this.name.hashCode()
				: this.name.toLowerCase().hashCode());
		result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
		result = 31 * result + (this.negated ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.value != null) {
			builder.append(this.name);
			if (this.negated) {
				builder.append('!');
			}
			builder.append('=');
			builder.append(this.value);
		}
		else {
			if (this.negated) {
				builder.append('!');
			}
			builder.append(this.name);
		}
		return builder.toString();
	}

}
