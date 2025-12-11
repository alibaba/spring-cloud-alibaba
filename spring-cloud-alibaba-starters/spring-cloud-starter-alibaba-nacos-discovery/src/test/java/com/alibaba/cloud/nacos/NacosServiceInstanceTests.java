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

package com.alibaba.cloud.nacos;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:uuuyuqi@gmail.com">uuuyuqi</a>
 */
public class NacosServiceInstanceTests {

	@Test
	public void toStringShouldContainAllFields() {
		NacosServiceInstance instance = new NacosServiceInstance();
		Map<String, String> metadata = new HashMap<>();
		metadata.put("zone", "sh");
		metadata.put("version", "1.0");

		instance.setServiceId("svc");
		instance.setInstanceId("instance-1");
		instance.setHost("127.0.0.1");
		instance.setPort(8080);
		instance.setSecure(true);
		instance.setMetadata(metadata);

		String text = instance.toString();

		assertThat(text).contains("NacosServiceInstance");
		assertThat(text).contains("serviceId", "svc");
		assertThat(text).contains("instanceId", "instance-1");
		assertThat(text).contains("host", "127.0.0.1");
		assertThat(text).contains("port", "8080");
		assertThat(text).contains("secure", "true");
		assertThat(text).contains("metadata", "zone", "sh", "version", "1.0");
	}

}


