/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.context;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AliCloudSpringApplicationTests.AliCloudDisabledApp.class, properties = {
		"spring.application.name=myapp",
		"spring.cloud.alicloud.edas.application.name=myapp",
		"spring.cloud.alicloud.access-key=ak", "spring.cloud.alicloud.secret-key=sk",
		"spring.cloud.alicloud.oss.endpoint=test",
		"spring.cloud.alicloud.scx.group-id=1-2-3-4",
		"spring.cloud.alicloud.edas.namespace=cn-test" }, webEnvironment = RANDOM_PORT)
@DirtiesContext
public class AliCloudSpringApplicationTests {

	@Test
	public void contextLoads() {
		System.out.println("Context load...");
	}

	@SpringBootApplication
	public static class AliCloudDisabledApp {

	}

}
