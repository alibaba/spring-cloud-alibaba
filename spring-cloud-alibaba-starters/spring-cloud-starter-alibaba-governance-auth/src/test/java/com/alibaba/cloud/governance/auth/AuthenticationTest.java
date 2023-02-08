/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.auth;

import java.io.File;
import java.nio.charset.Charset;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.auth.validator.AuthValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticationTest {

	@Autowired
	private AuthValidator authValidator;

	@Autowired
	private AuthRepository authRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void judgeFromIpAllow() throws Exception {
		AuthRepository authRepository = objectMapper.readValue(FileUtils.readFileToString(
				new File("src/test/resources/from-ip-allow.json"),
				Charset.defaultCharset()), AuthRepository.class);
		if (authRepository == null) {
			throw new Exception("Can not load auth rules from auth repository");
		}
		refreshAuthRepository(authRepository);
		AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder builder = new AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder();
		builder.setSourceIp("127.0.0.1");
		Assert.assertTrue(authValidator.validate(builder.build()));
		builder.setSourceIp("192.168.1.1");
		Assert.assertFalse(authValidator.validate(builder.build()));
	}

	@Test
	public void judgeMethodAllow() throws Exception {
		AuthRepository authRepository = objectMapper.readValue(FileUtils.readFileToString(
				new File("src/test/resources/method-allow.json"),
				Charset.defaultCharset()), AuthRepository.class);
		if (authRepository == null) {
			throw new Exception("Can not load auth rules from auth repository");
		}
		refreshAuthRepository(authRepository);
		AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder builder = new AuthValidator.UnifiedHttpRequest.UnifiedHttpRequestBuilder();
		builder.setMethod("HEAD");
		Assert.assertTrue(authValidator.validate(builder.build()));
		builder.setMethod("GET");
		Assert.assertTrue(authValidator.validate(builder.build()));
		builder.setMethod("POST");
		Assert.assertFalse(authValidator.validate(builder.build()));
		builder.setMethod("PUT");
		Assert.assertFalse(authValidator.validate(builder.build()));
	}

	private void refreshAuthRepository(AuthRepository authRepository) {
		this.authRepository.setDenyAuthRules(authRepository.getDenyAuthRules());
		this.authRepository.setAllowAuthRule(authRepository.getAllowAuthRules());
		this.authRepository.setJwtRule(authRepository.getJwtRules());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AuthValidatorAutoConfiguration.class })
	public static class TestConfig {

	}

}
