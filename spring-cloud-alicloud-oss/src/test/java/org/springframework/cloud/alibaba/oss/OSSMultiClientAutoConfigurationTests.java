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

package org.springframework.cloud.alibaba.oss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;

/**
 * Multi {@link OSS} {@link OSSProperties} Test
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OSSMultiClientAutoConfigurationTests {

	private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

	@Before
	public void init() {
		context.register(MultiClientConfiguration.class);
		EnvironmentTestUtils.addEnvironment(this.context,
				"spring.cloud.alibaba.oss.accessKeyId=your-ak",
				"spring.cloud.alibaba.oss.secretAccessKey=your-sk",
				"spring.cloud.alibaba.oss.endpoint=http://oss-cn-beijing.aliyuncs.com",
				"spring.cloud.alibaba.oss.configuration.userAgent=alibaba",
				"spring.cloud.alibaba.oss1.accessKeyId=your-ak1",
				"spring.cloud.alibaba.oss1.secretAccessKey=your-sk1",
				"spring.cloud.alibaba.oss1.endpoint=http://oss-cn-beijing.aliyuncs.com",
				"spring.cloud.alibaba.oss1.configuration.userAgent=alibaba1");
		this.context.refresh();
	}

	@Test
	public void testOSSClient() {
		assertThat(context.getBeansOfType(OSS.class).size()).isEqualTo(2);
		OSSClient ossClient = (OSSClient) context.getBean("ossClient1", OSS.class);
		assertThat(ossClient.getCredentialsProvider().getCredentials().getAccessKeyId())
				.isEqualTo("your-ak");
		assertThat(
				ossClient.getCredentialsProvider().getCredentials().getSecretAccessKey())
						.isEqualTo("your-sk");
		assertThat(ossClient.getEndpoint().toString())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossClient.getClientConfiguration().getUserAgent())
				.isEqualTo("alibaba");
		OSSClient ossClient1 = (OSSClient) context.getBean("ossClient2", OSS.class);
		assertThat(ossClient1.getCredentialsProvider().getCredentials().getAccessKeyId())
				.isEqualTo("your-ak1");
		assertThat(
				ossClient1.getCredentialsProvider().getCredentials().getSecretAccessKey())
						.isEqualTo("your-sk1");
		assertThat(ossClient1.getEndpoint().toString())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossClient1.getClientConfiguration().getUserAgent())
				.isEqualTo("alibaba1");
	}

	@Configuration
	@EnableConfigurationProperties
	protected static class MultiClientConfiguration {

		@Bean
		@ConfigurationProperties(prefix = "spring.cloud.alibaba.oss")
		public OSSProperties ossProperties1() {
			return new OSSProperties();
		}

		@Bean
		public OSS ossClient1(@Qualifier("ossProperties1") OSSProperties ossProperties) {
			return new OSSClientBuilder().build(ossProperties.getEndpoint(),
					ossProperties.getAccessKeyId(), ossProperties.getSecretAccessKey(),
					ossProperties.getSecurityToken(), ossProperties.getConfiguration());
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.cloud.alibaba.oss1")
		public OSSProperties ossProperties2() {
			return new OSSProperties();
		}

		@Bean
		public OSS ossClient2(@Qualifier("ossProperties2") OSSProperties ossProperties) {
			return new OSSClientBuilder().build(ossProperties.getEndpoint(),
					ossProperties.getAccessKeyId(), ossProperties.getSecretAccessKey(),
					ossProperties.getSecurityToken(), ossProperties.getConfiguration());
		}

	}

}