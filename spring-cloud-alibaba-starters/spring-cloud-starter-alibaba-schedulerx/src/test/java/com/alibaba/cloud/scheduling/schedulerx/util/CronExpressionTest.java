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

package com.alibaba.cloud.scheduling.schedulerx.util;

import java.text.ParseException;
import java.util.Date;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yaohui
 */
class CronExpressionTest {

	@Test
	void isValidExpression() {
		assertThat(CronExpression.isValidExpression("0 0 0 * * ?")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 */5 * * * ?")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 0 8 1 JAN ?")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 THU")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 THU-SAT")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 FRI#1")).isEqualTo(true);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 FRI#5")).isEqualTo(true);
		// false
		assertThat(CronExpression.isValidExpression("0 0 8 1 JAW ?")).isEqualTo(false);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 THP-SAT")).isEqualTo(false);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 FRI#0")).isEqualTo(false);
		assertThat(CronExpression.isValidExpression("0 0 8 ? 10 FRI#6")).isEqualTo(false);
	}

	@Test
	void getTimeAfter() throws ParseException {
		CronExpression cronExpression = new CronExpression("0 0 8 1 JAN ?");
		Date nextDate = cronExpression.getTimeAfter(new Date());
		System.out.println(nextDate);
		assertThat(nextDate).isNotNull();

		cronExpression = new CronExpression("0 */5 * * * ?");
		nextDate = cronExpression.getTimeAfter(new Date());
		System.out.println(nextDate);
		assertThat(nextDate).isNotNull();
	}

	@Test
	void getTimeBefore() throws ParseException {
		CronExpression cronExpression = new CronExpression("0 0 8 1 JAN ?");
		Date beforeDate = cronExpression.getTimeBefore(new Date());
		System.out.println(beforeDate);
		assertThat(beforeDate).isNotNull();

		cronExpression = new CronExpression("0 */5 * * * ?");
		beforeDate = cronExpression.getTimeBefore(new Date());
		System.out.println(beforeDate);
		assertThat(beforeDate).isNotNull();
	}

}
