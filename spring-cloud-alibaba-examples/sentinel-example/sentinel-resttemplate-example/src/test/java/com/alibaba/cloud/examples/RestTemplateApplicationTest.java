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

package com.alibaba.cloud.examples;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test {@link RestTemplateApplication}.
 *
 * @author wangliang181230
 */
@SpringBootTest
@Disabled("For debugging")
public class RestTemplateApplicationTest {

	@Test
	public void runWithSpringAotMode() throws Exception {
		System.setProperty("spring.aot.enabled", "true");
		RestTemplateApplication.main(new String[0]);
	}

}
