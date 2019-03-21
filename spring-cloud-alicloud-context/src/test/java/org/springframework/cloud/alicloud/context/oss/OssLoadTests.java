/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alicloud.context.oss;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alicloud.context.AliCloudContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.cloud.alicloud.context.ans.AnsContextAutoConfiguration;
import org.springframework.cloud.alicloud.context.edas.EdasContextAutoConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.aliyun.oss.OSSClient;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AliCloudContextAutoConfiguration.class,
		EdasContextAutoConfiguration.class, AnsContextAutoConfiguration.class,
		OssContextAutoConfiguration.class }, properties = {
				"spring.cloud.alicloud.accessKey=your-ak",
				"spring.cloud.alicloud.secretKey=your-sk",
				"spring.cloud.alicloud.oss.endpoint=http://oss-cn-beijing.aliyuncs.com",
				"spring.cloud.alicloud.oss.config.userAgent=alibaba" })
public class OssLoadTests {

	@Autowired
	private OssProperties ossProperties;

	@Autowired
	private AliCloudProperties aliCloudProperties;

	@Autowired
	private OSSClient ossClient;

	@Test
	public void testProperties() {
		assertThat(aliCloudProperties.getAccessKey()).isEqualTo("your-ak");
		assertThat(aliCloudProperties.getSecretKey()).isEqualTo("your-sk");
		assertThat(ossProperties.getEndpoint())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossProperties.getConfig().getUserAgent()).isEqualTo("alibaba");
	}

	@Test
	public void testClient() {
		assertThat(ossClient.getEndpoint().toString())
				.isEqualTo("http://oss-cn-beijing.aliyuncs.com");
		assertThat(ossClient.getClientConfiguration().getUserAgent())
				.isEqualTo("alibaba");
		assertThat(ossClient.getCredentialsProvider().getCredentials().getAccessKeyId())
				.isEqualTo("your-ak");
		assertThat(
				ossClient.getCredentialsProvider().getCredentials().getSecretAccessKey())
						.isEqualTo("your-sk");
	}
}
