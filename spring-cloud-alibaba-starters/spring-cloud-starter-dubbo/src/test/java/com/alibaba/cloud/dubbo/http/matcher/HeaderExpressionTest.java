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

import org.junit.Assert;
import org.junit.Test;

import org.springframework.http.HttpRequest;

import static com.alibaba.cloud.dubbo.http.DefaultHttpRequest.builder;

/**
 * {@link HeaderExpression} Test.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HeaderExpressionTest
		extends AbstractNameValueExpressionTest<HeaderExpression> {

	@Test
	public void testIsCaseSensitiveName() {
		Assert.assertFalse(createExpression("a=1").isCaseSensitiveName());
		Assert.assertFalse(createExpression("a=!1").isCaseSensitiveName());
		Assert.assertFalse(createExpression("b=1").isCaseSensitiveName());
	}

	@Test
	public void testMatch() {

		HeaderExpression expression = createExpression("a=1");
		HttpRequest request = builder().build();

		Assert.assertFalse(expression.match(request));

		request = builder().header("a", "").build();
		Assert.assertFalse(expression.match(request));

		request = builder().header("a", "2").build();
		Assert.assertFalse(expression.match(request));

		request = builder().header("", "1").build();
		Assert.assertFalse(expression.match(request));

		request = builder().header("a", "1").build();
		Assert.assertTrue(expression.match(request));
	}

}
