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

package org.springframework.cloud.alicloud.context.scx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.alicloud.context.edas.EdasProperties;
import org.springframework.cloud.alicloud.context.oss.OssProperties;

import com.aliyun.oss.OSS;

/**
 * {@link OSS} {@link OssProperties} Test
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ScxAutoConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ScxContextAutoConfiguration.class))
			.withPropertyValues("spring.cloud.alicloud.scx.group-id=1-2-3-4")
			.withPropertyValues("spring.cloud.alicloud.edas.namespace=cn-test");

	@Test
	public void testSxcProperties() {
		this.contextRunner.run(context -> {
			assertThat(context.getBeansOfType(ScxProperties.class).size() == 1).isTrue();
			EdasProperties edasProperties = context.getBean(EdasProperties.class);
			ScxProperties scxProperties = context.getBean(ScxProperties.class);
			assertThat(scxProperties.getGroupId()).isEqualTo("1-2-3-4");
			assertThat(edasProperties.getNamespace()).isEqualTo("cn-test");
			assertThat(scxProperties.getDomainName()).isNull();
		});
	}

}
