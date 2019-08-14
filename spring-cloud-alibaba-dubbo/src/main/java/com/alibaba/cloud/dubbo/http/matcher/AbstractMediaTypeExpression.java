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

import org.springframework.http.MediaType;

/**
 * The some source code is scratched from
 * org.springframework.web.servlet.mvc.condition.AbstractMediaTypeExpression
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class AbstractMediaTypeExpression
		implements MediaTypeExpression, Comparable<AbstractMediaTypeExpression> {

	private final MediaType mediaType;

	private final boolean negated;

	AbstractMediaTypeExpression(String expression) {
		if (expression.startsWith("!")) {
			this.negated = true;
			expression = expression.substring(1);
		}
		else {
			this.negated = false;
		}
		this.mediaType = MediaType.parseMediaType(expression);
	}

	AbstractMediaTypeExpression(MediaType mediaType, boolean negated) {
		this.mediaType = mediaType;
		this.negated = negated;
	}

	@Override
	public MediaType getMediaType() {
		return this.mediaType;
	}

	@Override
	public boolean isNegated() {
		return this.negated;
	}

	@Override
	public int compareTo(AbstractMediaTypeExpression other) {
		return MediaType.SPECIFICITY_COMPARATOR.compare(this.getMediaType(),
				other.getMediaType());
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || getClass() != other.getClass()) {
			return false;
		}
		AbstractMediaTypeExpression otherExpr = (AbstractMediaTypeExpression) other;
		return (this.mediaType.equals(otherExpr.mediaType)
				&& this.negated == otherExpr.negated);
	}

	@Override
	public int hashCode() {
		return this.mediaType.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.negated) {
			builder.append('!');
		}
		builder.append(this.mediaType.toString());
		return builder.toString();
	}
}
