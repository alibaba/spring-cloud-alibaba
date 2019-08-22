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

/**
 * {@link AbstractNameValueExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public abstract class AbstractNameValueExpressionTest<T extends AbstractNameValueExpression> {

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
	public void testGetNameAndValue() {
		// Normal Name and value
		AbstractNameValueExpression expression = createExpression("a=1");
		Assert.assertEquals("a", expression.getName());
		Assert.assertFalse(expression.isNegated());

		expression = createExpression("a=1");
		Assert.assertEquals("a", expression.getName());
		Assert.assertEquals("1", expression.getValue());
		Assert.assertFalse(expression.isNegated());

		// Negated Name
		expression = createExpression("!a");
		Assert.assertEquals("a", expression.getName());
		Assert.assertTrue(expression.isNegated());

		expression = createExpression("a!=1");
		Assert.assertEquals("a", expression.getName());
		Assert.assertEquals("1", expression.getValue());
		Assert.assertTrue(expression.isNegated());
	}

	@Test
	public void testEqualsAndHashCode() {
		Assert.assertEquals(createExpression("a"), createExpression("a"));
		Assert.assertEquals(createExpression("a").hashCode(),
				createExpression("a").hashCode());
		Assert.assertEquals(createExpression("a=1"), createExpression("a = 1 "));
		Assert.assertEquals(createExpression("a=1").hashCode(),
				createExpression("a = 1 ").hashCode());
		Assert.assertNotEquals(createExpression("a"), createExpression("b"));
		Assert.assertNotEquals(createExpression("a").hashCode(),
				createExpression("b").hashCode());
	}
}
