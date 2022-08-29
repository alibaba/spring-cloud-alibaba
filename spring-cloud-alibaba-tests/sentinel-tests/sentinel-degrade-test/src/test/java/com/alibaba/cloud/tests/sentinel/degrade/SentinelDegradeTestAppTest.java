package com.alibaba.cloud.tests.sentinel.degrade;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SentinelDegradeTestAppTest {

	@LocalServerPort
	int port;

	@Test
	public void testDegradeRule() {
		RestTemplate rest = new RestTemplate();

		ResponseEntity<String> res = rest
				.getForEntity("http://localhost:" + port + "/degrade", String.class);

		assertThat(res.getBody()).contains("fallback");
	}

}
