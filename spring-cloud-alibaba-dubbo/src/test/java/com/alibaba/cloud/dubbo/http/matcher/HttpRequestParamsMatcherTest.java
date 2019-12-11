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

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import org.springframework.mock.http.client.MockClientHttpRequest;

/**
 * {@link HttpRequestParamsMatcher} Test.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpRequestParamsMatcherTest {

	// @Test
	// public void testGetParams() {
	//
	// HttpRequestParamsMatcher matcher = new HttpRequestParamsMatcher(
	// "a ",
	// "a =1",
	// "b = 2",
	// "b = 3 ",
	// " c = 4 ",
	// " d"
	// );
	//
	// Map<String, List<String>> params = matcher.getParams();
	// Assert.assertEquals(4, params.size());
	// Assert.assertTrue(params.containsKey("a"));
	// Assert.assertTrue(params.containsKey("b"));
	// Assert.assertTrue(params.containsKey("c"));
	// Assert.assertTrue(params.containsKey("d"));
	// Assert.assertFalse(params.containsKey("e"));
	//
	// List<String> values = params.get("a");
	// Assert.assertEquals(2, values.size());
	// Assert.assertEquals("", values.get(0));
	// Assert.assertEquals("1", values.get(1));
	//
	// values = params.get("b");
	// Assert.assertEquals(2, values.size());
	// Assert.assertEquals("2", values.get(0));
	// Assert.assertEquals("3", values.get(1));
	//
	// values = params.get("c");
	// Assert.assertEquals(1, values.size());
	// Assert.assertEquals("4", values.get(0));
	//
	// values = params.get("d");
	// Assert.assertEquals(1, values.size());
	// Assert.assertEquals("", values.get(0));
	// }

	@Test
	public void testEquals() {

		HttpRequestParamsMatcher matcher = new HttpRequestParamsMatcher("a  ", "a = 1");

		MockClientHttpRequest request = new MockClientHttpRequest();

		request.setURI(URI.create("http://dummy/?a"));
		Assert.assertTrue(matcher.match(request));
		request.setURI(URI.create("http://dummy/?a&a=1"));
		Assert.assertTrue(matcher.match(request));

		matcher = new HttpRequestParamsMatcher("a  ", "a =1", "b", "b=2");
		request.setURI(URI.create("http://dummy/?a&a=1&b"));
		Assert.assertTrue(matcher.match(request));
		request.setURI(URI.create("http://dummy/?a&a=1&b&b=2"));
		Assert.assertTrue(matcher.match(request));

		matcher = new HttpRequestParamsMatcher("a  ", "a =1", "b", "b=2", "b = 3 ");
		request.setURI(URI.create("http://dummy/?a&a=1&b&b=2&b=3"));
		Assert.assertTrue(matcher.match(request));

		request.setURI(URI.create("http://dummy/?d=1"));
		Assert.assertFalse(matcher.match(request));
	}

}
