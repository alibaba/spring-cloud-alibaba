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

package com.alibaba.cloud.integration.business.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.alibaba.cloud.integration.business.util.HttpClientUtils;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author TrevorLink
 */
@RestController
@RequestMapping("/praise")
public class PraiseBusinessController {

	/**
	 * Simulate high traffic for rocketmq.
	 */
	public static final int HIGH_TRAFFIC_ORDER_ROCKETMQ = 1000;

	/**
	 * Simulate low traffic for sentinel.
	 */
	public static final int HIGH_TRAFFIC_ORDER_SENTINEL = 10;

	/**
	 * Sentinel request but flow restricted results.
	 */
	public List<String> res = Collections.synchronizedList(new ArrayList<>());

	@RequestMapping({ "/rocketmq" })
	public void rocketmq(@RequestParam("itemId") Integer itemId) {
		try {
			startTaskAllInOnce(HIGH_TRAFFIC_ORDER_ROCKETMQ, () -> HttpClientUtils
					.doGet("http://localhost:8010/praise/rocketmq?itemId=" + itemId));
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping({ "/sentinel" })
	public List<String> sentinel(@RequestParam("itemId") Integer itemId) {
		try {
			startTaskAllInOnce(HIGH_TRAFFIC_ORDER_SENTINEL, () -> {
				res.add(HttpClientUtils
						.doGet("http://localhost:8010/praise/sentinel?itemId=" + itemId));
			});
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		return res;
	}

	private void startTaskAllInOnce(int threadNums, final Runnable task)
			throws InterruptedException {
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(threadNums);
		for (int i = 0; i < threadNums; i++) {
			Thread t = new Thread() {
				@Override
				public void run() {
					try {
						startGate.await();
						try {
							task.run();
						}
						finally {
							endGate.countDown();
						}
					}
					catch (InterruptedException ie) {
						ie.printStackTrace();
					}
				}
			};
			t.start();
		}
		startGate.countDown();
		endGate.await();
	}

}
