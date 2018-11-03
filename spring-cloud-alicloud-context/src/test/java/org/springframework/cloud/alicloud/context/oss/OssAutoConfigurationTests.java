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

package org.springframework.cloud.alicloud.context.oss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.alicloud.context.AliCloudProperties;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;

/**
 * {@link OSS} {@link OssProperties} Test
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class OssAutoConfigurationTests {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(OssContextAutoConfiguration.class))
			.withPropertyValues("spring.cloud.alicloud.accessKey=your-ak")
			.withPropertyValues("spring.cloud.alicloud.secretKey=your-sk")
			.withPropertyValues(
					"spring.cloud.alicloud.oss.endpoint=http://oss-cn-beijing.aliyuncs.com")
			.withPropertyValues("spring.cloud.alicloud.oss.config.userAgent=alibaba");

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
		});
	}

	@Test
	public void testOSSClient() {
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

}
