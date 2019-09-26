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

package com.alibaba.alicloud.context.oss;

import com.alibaba.alicloud.context.AliCloudProperties;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import org.junit.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OSS} {@link OssProperties} Test
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OssAutoConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(OssContextAutoConfiguration.class))
			.withPropertyValues("spring.cloud.alicloud.accessKey=your-ak",
					"spring.cloud.alicloud.secretKey=your-sk",
					"spring.cloud.alicloud.oss.endpoint=http://oss-cn-beijing.aliyuncs.com",
					"spring.cloud.alicloud.oss.config.userAgent=alibaba",
					"spring.cloud.alicloud.oss.sts.access-key=your-sts-ak",
					"spring.cloud.alicloud.oss.sts.secret-key=your-sts-sk",
					"spring.cloud.alicloud.oss.sts.security-token=your-sts-token");

	@Test
	public void testOSSProperties() {
		this.contextRunner.run(context -> {
			assertThat(context.getBeansOfType(OssProperties.class).size() == 1).isTrue();
			AliCloudProperties aliCloudProperties = context
					.getBean(AliCloudProperties.class);
			OssProperties ossProperties = context.getBean(OssProperties.class);
			assertThat(aliCloudProperties.getAccessKey()).isEqualTo("your-ak");
			assertThat(aliCloudProperties.getSecretKey()).isEqualTo("your-sk");
			assertThat(ossProperties.getEndpoint())
					.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
			assertThat(ossProperties.getConfig().getUserAgent()).isEqualTo("alibaba");
			assertThat(ossProperties.getSts().getAccessKey()).isEqualTo("your-sts-ak");
			assertThat(ossProperties.getSts().getSecretKey()).isEqualTo("your-sts-sk");
			assertThat(ossProperties.getSts().getSecurityToken())
					.isEqualTo("your-sts-token");
		});
	}

	@Test
	public void testOSSClient1() {
		this.contextRunner.run(context -> {
			assertThat(context.getBeansOfType(OSS.class).size() == 1).isTrue();
			assertThat(context.getBeanNamesForType(OSS.class)[0]).isEqualTo("ossClient");
			OSSClient ossClient = (OSSClient) context.getBean(OSS.class);
			assertThat(ossClient.getEndpoint().toString())
					.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
			assertThat(ossClient.getClientConfiguration().getUserAgent())
					.isEqualTo("alibaba");
			assertThat(
					ossClient.getCredentialsProvider().getCredentials().getAccessKeyId())
							.isEqualTo("your-ak");
			assertThat(ossClient.getCredentialsProvider().getCredentials()
					.getSecretAccessKey()).isEqualTo("your-sk");
		});
	}

	@Test
	public void testOSSClient2() {
		this.contextRunner
				.withPropertyValues("spring.cloud.alicloud.oss.authorization-mode=STS")
				.run(context -> {
					assertThat(context.getBeansOfType(OSS.class).size() == 1).isTrue();
					assertThat(context.getBeanNamesForType(OSS.class)[0])
							.isEqualTo("ossClient");
					OSSClient ossClient = (OSSClient) context.getBean(OSS.class);
					assertThat(ossClient.getEndpoint().toString())
							.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
					assertThat(ossClient.getClientConfiguration().getUserAgent())
							.isEqualTo("alibaba");
					assertThat(ossClient.getCredentialsProvider().getCredentials()
							.getAccessKeyId()).isEqualTo("your-sts-ak");
					assertThat(ossClient.getCredentialsProvider().getCredentials()
							.getSecretAccessKey()).isEqualTo("your-sts-sk");
					assertThat(ossClient.getCredentialsProvider().getCredentials()
							.getSecurityToken()).isEqualTo("your-sts-token");
				});
	}

}
