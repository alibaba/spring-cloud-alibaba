/*
 * Copyright 2024-present the original author or authors.
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

package com.alibaba.cloud.examples.schedule.job;

import java.util.concurrent.TimeUnit;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author yaohui
 **/
@Component
public class SimpleJob {

	private static final Logger logger = LoggerFactory.getLogger(SimpleJob.class);

	/**
	 * run without lock, all instance running at the same time.
	 */
	@Scheduled(cron = "0 */1 * * * ?")
	public void job1() {
		logger.info("time=" + DateTime.now().toString("YYYY-MM-dd HH:mm:ss") + " do job1...");
	}


	/**
	 * run with lock, only one instance running at the same time.
	 *
	 * @throws InterruptedException interrupted exception
	 */
	@Scheduled(cron = "0 */1 * * * ?")
	@SchedulerLock(name = "lock-job2", lockAtMostFor = "10s")
	public void job2() throws InterruptedException {
		logger.info("time=" + DateTime.now().toString("YYYY-MM-dd HH:mm:ss") + " do job2...");
		TimeUnit.SECONDS.sleep(1L);
	}
}
