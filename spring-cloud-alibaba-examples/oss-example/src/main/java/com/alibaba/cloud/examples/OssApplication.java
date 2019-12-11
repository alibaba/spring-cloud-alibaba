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

package com.alibaba.cloud.examples;

import java.net.URISyntaxException;

import com.aliyun.oss.OSS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * OSS Application.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
public class OssApplication {

	/**
	 * Bucket Name of OSS Example.
	 */
	public static final String BUCKET_NAME = "spring-cloud-alibaba-test";

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(OssApplication.class, args);
	}

	@Bean
	public AppRunner appRunner() {
		return new AppRunner();
	}

	class AppRunner implements ApplicationRunner {

		@Autowired
		private OSS ossClient;

		@Override
		public void run(ApplicationArguments args) throws Exception {
			try {
				if (!ossClient.doesBucketExist(BUCKET_NAME)) {
					ossClient.createBucket(BUCKET_NAME);
				}
			}
			catch (Exception e) {
				System.err.println("oss handle bucket error: " + e.getMessage());
				System.exit(-1);
			}
		}

	}

}
