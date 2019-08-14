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

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;

/**
 * {@link AbstractMediaTypeExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractMediaTypeExpressionTest<T extends AbstractMediaTypeExpression> {

	protected T createExpression(String expression) {
		ResolvableType resolvableType = ResolvableType
				.forType(getClass().getGenericSuperclass());
		Class<T> firstGenericType = (Class<T>) resolvableType.resolveGeneric(0);
		Constructor<T> constructor = null;
		try {
			constructor = firstGenericType.getDeclaredConstructor(String.class);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		return BeanUtils.instantiateClass(constructor, expression);
	}

	@Test
	public void testGetMediaTypeAndNegated() {
		// Normal
		AbstractMediaTypeExpression expression = createExpression(
				MediaType.APPLICATION_JSON_VALUE);
		Assert.assertEquals(MediaType.APPLICATION_JSON, expression.getMediaType());
		Assert.assertFalse(expression.isNegated());

		// Negated
		expression = createExpression("!" + MediaType.APPLICATION_JSON_VALUE);
		Assert.assertEquals(MediaType.APPLICATION_JSON, expression.getMediaType());
		Assert.assertTrue(expression.isNegated());
	}

	@Test
	public void testEqualsAndHashCode() {
		Assert.assertEquals(createExpression(MediaType.APPLICATION_JSON_VALUE),
				createExpression(MediaType.APPLICATION_JSON_VALUE));
		Assert.assertEquals(createExpression(MediaType.APPLICATION_JSON_VALUE).hashCode(),
				createExpression(MediaType.APPLICATION_JSON_VALUE).hashCode());
	}

	@Test
	public void testCompareTo() {
		Assert.assertEquals(0, createExpression(MediaType.APPLICATION_JSON_VALUE)
				.compareTo(createExpression(MediaType.APPLICATION_JSON_VALUE)));
	}
}
