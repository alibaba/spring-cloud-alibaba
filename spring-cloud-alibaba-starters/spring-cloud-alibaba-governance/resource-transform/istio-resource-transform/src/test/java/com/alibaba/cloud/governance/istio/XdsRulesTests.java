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

package com.alibaba.cloud.governance.istio;

import java.lang.reflect.Field;
import java.util.Map;

import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.router.data.ControlPlaneAutoConfiguration;
import com.alibaba.fastjson.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XdsRulesTests.TestConfig.class, properties = {
		"spring.cloud.istio.config.host=39.105.35.234",
		"spring.cloud.istio.config.port=15010", "spring.cloud.istio.config.enabled=true",
		"spring.cloud.istio.config.polling-pool-size=10",
		"spring.cloud.istio.config.istiod-token=eyJhbGciOiJSUzI1NiIsImtpZCI6IlJPazJiMERoVGZiTExDbjBnUnBoRGtFLWxjbzl6cTFoYjZtQ1lKWWluN1UifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJpc3Rpby1zeXN0ZW0iLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlY3JldC5uYW1lIjoiaXN0aW9kLXRva2VuLWpjZ3dsIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImlzdGlvZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VydmljZS1hY2NvdW50LnVpZCI6IjY4NjM5NzQ1LTQzN2QtNGNiNi1iNDk5LWE0YmJiOGRhYmZlMSIsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDppc3Rpby1zeXN0ZW06aXN0aW9kIn0.mDoEEgkUVoVmOn5OZA9xIH15rMT5iUPpUYF0DediJJO2HfDatTYl1Jcl9UmEyjDzuTQsR69yHXR1mzjpHseWGmVAMwOYDcfIY_0ljWZ_CprUmY3zDygL7_cG_gsM3BzMCzsH8U8coWisLHxZ2iPLmLNGlZDRhWIGiAatmT5wFbgq9hP9DJ1Rr_FDBT5US_8l9-UNf0SsrDNO11ImFWQVdPxbyrdDqd4JQGNKlVDVRCdC6dhalzhGVMDnpc1ZFQ5Audxki_LoVfOiigzFsbceOCP4a6bJ9VNbbNggC2vAzG79fNhcobTZY4sQY2Kwjl5D33hLS-uuqPInpg6wXXmBUw",
		"spring.cloud.istio.config.polling-time=30" }, webEnvironment = NONE)
public class XdsRulesTests {

	private static final Logger log = LoggerFactory.getLogger(XdsRulesTests.class);

	@Autowired
	private AuthRepository authRepository;

	@BeforeClass
	public static void setEnv() {
		// init some envi
		setEnv(IstioConstants.POD_NAME, "sca");
		setEnv(IstioConstants.NAMESPACE_NAME, "auth");
	}

	@Test
	public void testRBACRules() {
		log.info("target rules: {}", JSONObject.toJSONString(authRepository));
	}

	private static void setEnv(String key, String value) {
		try {
			Map<String, String> env = System.getenv();
			Class<?> cl = env.getClass();
			Field field = cl.getDeclaredField("m");
			field.setAccessible(true);
			Map<String, String> writableEnv = (Map<String, String>) field.get(env);
			writableEnv.put(key, value);
		}
		catch (Exception e) {
			throw new IllegalStateException("Failed to set environment variable", e);
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ XdsAutoConfiguration.class,
			ControlPlaneAutoConfiguration.class })
	public static class TestConfig {

	}

}
