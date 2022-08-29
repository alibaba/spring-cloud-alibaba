package com.alibaba.cloud.tests.sentinel.degrade;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class SentinelFlowControlTestAppTest {

	@LocalServerPort
	int port;

	@Test
	public void testFlowControl() throws InterruptedException {
		RestTemplate rest = new RestTemplate();

		final int threadCount = 3;
		CountDownLatch latch = new CountDownLatch(threadCount);
		List<String> result = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			new Thread(() -> {
				try {
					ResponseEntity<String> res = rest.getForEntity(
							"http://localhost:" + port + "/flowControl", String.class);
					result.add(res.getBody());
				} finally {
					latch.countDown();
				}
			}).start();
		}

		latch.await();

		assertThat(result).anyMatch(s -> s.contains("fallback"));
	}

}
