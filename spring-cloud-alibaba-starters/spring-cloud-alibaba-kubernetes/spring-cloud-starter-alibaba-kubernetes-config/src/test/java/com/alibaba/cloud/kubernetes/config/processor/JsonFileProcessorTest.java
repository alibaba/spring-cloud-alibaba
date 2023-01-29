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

package com.alibaba.cloud.kubernetes.config.processor;

import org.junit.jupiter.api.Test;

import org.springframework.core.env.EnumerablePropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link JsonFileProcessor} tester.
 */
class JsonFileProcessorTest {

	/**
	 * {@link JsonFileProcessor#generate(String, String)}.
	 */
	@Test
	void generate_whenJsonObject() {
		String json = "{\n" + "  \"username\": \"admin\",\n"
				+ "  \"password\": \"666\",\n" + "  \"hobbies\": [\n"
				+ "    \"reading\",\n" + "    \"writing\"\n" + "  ]\n" + "}\n";
		EnumerablePropertySource<?> ps = new JsonFileProcessor().generate("test_generate",
				json);

		assertThat(ps.getPropertyNames()).hasSize(4);
		assertThat(ps.getProperty("username")).isEqualTo("admin");
		assertThat(ps.getProperty("password")).isEqualTo("666");
		assertThat(ps.getProperty("hobbies[0]")).isEqualTo("reading");
		assertThat(ps.getProperty("hobbies[1]")).isEqualTo("writing");
	}

	/**
	 * {@link JsonFileProcessor#generate(String, String)}.
	 */
	@Test
	void generate_whenJsonArray() {
		String jsonArray = "[\n" + "  {\n" + "    \"username\": \"admin\",\n"
				+ "    \"password\": \"666\",\n" + "    \"hobbies\": [\n"
				+ "      \"reading\",\n" + "      \"writing\"\n" + "    ]\n" + "  },\n"
				+ "  {\n" + "    \"username\": \"root\",\n"
				+ "    \"password\": \"888\",\n" + "    \"hobbies\": [\n"
				+ "      \"reading\",\n" + "      \"writing\",\n" + "      \"coding\"\n"
				+ "    ]\n" + "  }\n" + "]\n";
		EnumerablePropertySource<?> ps = new JsonFileProcessor().generate("test_generate",
				jsonArray);

		// previous config wins
		assertThat(ps.getPropertyNames()).hasSize(5);
		assertThat(ps.getProperty("username")).isEqualTo("admin");
		assertThat(ps.getProperty("password")).isEqualTo("666");
		assertThat(ps.getProperty("hobbies[0]")).isEqualTo("reading");
		assertThat(ps.getProperty("hobbies[1]")).isEqualTo("writing");
		assertThat(ps.getProperty("hobbies[2]")).isEqualTo("coding");
	}
}
