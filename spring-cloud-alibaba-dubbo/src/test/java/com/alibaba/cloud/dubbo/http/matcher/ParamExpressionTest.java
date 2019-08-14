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

import static com.alibaba.cloud.dubbo.http.DefaultHttpRequest.builder;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpRequest;

/**
 * {@link ParamExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class ParamExpressionTest
		extends AbstractNameValueExpressionTest<ParamExpression> {

	@Test
	public void testIsCaseSensitiveName() {
		Assert.assertTrue(createExpression("a=1").isCaseSensitiveName());
		Assert.assertTrue(createExpression("a=!1").isCaseSensitiveName());
		Assert.assertTrue(createExpression("b=1").isCaseSensitiveName());
	}

	@Test
	public void testMatch() {

		ParamExpression expression = createExpression("a=1");
		HttpRequest request = builder().build();

		Assert.assertFalse(expression.match(request));

		request = builder().param("a", "").build();
		Assert.assertFalse(expression.match(request));

		request = builder().param("a", "2").build();
		Assert.assertFalse(expression.match(request));

		request = builder().param("", "1").build();
		Assert.assertFalse(expression.match(request));

		request = builder().param("a", "1").build();
		Assert.assertTrue(expression.match(request));
	}

}
