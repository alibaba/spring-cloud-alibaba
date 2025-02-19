/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ByteArrayResource;

/**
 * NacosJsonPropertySourceLoaderTests Tester.
 *
 * @author ruansheng
 */
public class NacosJsonPropertySourceLoaderTests {

	@Test
	public void testJsonPropWithComment() throws IOException {
		String propJsonStr = """
				{
					// test comment
					"name": "jack"
				}
				""";
		ByteArrayResource resource = new ByteArrayResource(propJsonStr.getBytes());
		List<PropertySource<?>> result = new NacosJsonPropertySourceLoader().doLoad("test.json", resource);
		Assertions.assertThat(result).hasSize(1);
		Assertions.assertThat(result.get(0).getProperty("name")).isEqualTo("jack");
	}
}
