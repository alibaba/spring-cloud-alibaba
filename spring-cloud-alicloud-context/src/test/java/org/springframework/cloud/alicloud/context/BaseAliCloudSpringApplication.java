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

import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiaolongzuo
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore("javax.management.*")
@SpringBootTest(classes = BaseAliCloudSpringApplication.AliCloudApplication.class, properties = {
		"spring.application.name=myapp",
		"spring.cloud.alicloud.edas.application.name=myapp",
		"spring.cloud.alicloud.access-key=ak", "spring.cloud.alicloud.secret-key=sk",
		"spring.cloud.alicloud.oss.endpoint=test",
		"spring.cloud.alicloud.scx.group-id=1-2-3-4",
		"spring.cloud.alicloud.edas.namespace=cn-test",
		"spring.cloud.alicloud.ans.server-list=192.168.1.100",
		"spring.cloud.alicloud.ans.server-port=8888",
		"spring.cloud.alicloud.oss.enabled=false",
		"spring.cloud.alicloud.scx.enabled=false" })
public abstract class BaseAliCloudSpringApplication {

	@SpringBootApplication
	public static class AliCloudApplication {

	}

}
