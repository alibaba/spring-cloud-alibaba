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

package org.springframework.cloud.alibaba.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;

/**
 * {@link OSS} {@link OSSProperties} Test
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OSSAutoConfigurationTests {

	private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@Before
	public void init() {
		context.register(OSSAutoConfiguration.class);
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.cloud.alibaba.oss.accessKeyId=your-ak",
				"spring.cloud.alibaba.oss.secretAccessKey=your-sk",
				"spring.cloud.alibaba.oss.endpoint=http://oss-cn-beijing.aliyuncs.com",
				"spring.cloud.alibaba.oss.configuration.userAgent=alibaba");
		this.context.refresh();
	}

	@Test
	public void testOSSProperties() {
		OSSProperties ossProperties = context.getBean(OSSProperties.class);
		assertThat(ossProperties.getAccessKeyId()).isEqualTo("your-ak");
		assertThat(ossProperties.getSecretAccessKey()).isEqualTo("your-sk");
		assertThat(ossProperties.getEndpoint())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossProperties.getConfiguration().getUserAgent()).isEqualTo("alibaba");
	}

	@Test
	public void testOSSClient() {
		assertThat(context.getBeansOfType(OSS.class).size()).isEqualTo(1);
		assertThat(context.getBeanNamesForType(OSS.class)[0]).isEqualTo("ossClient");
		OSSClient ossClient = (OSSClient) context.getBean(OSS.class);
		assertThat(ossClient.getCredentialsProvider().getCredentials().getAccessKeyId())
				.isEqualTo("your-ak");
		assertThat(
				ossClient.getCredentialsProvider().getCredentials().getSecretAccessKey())
						.isEqualTo("your-sk");
		assertThat(ossClient.getEndpoint().toString())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossClient.getClientConfiguration().getUserAgent())
				.isEqualTo("alibaba");
	}

}