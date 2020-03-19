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

import java.util.Random;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaojing
 */
@RestController
public class AccountController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

	private static final String SUCCESS = "SUCCESS";

	private static final String FAIL = "FAIL";

	private final JdbcTemplate jdbcTemplate;

	private Random random;

	public AccountController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.random = new Random();
	}

	@PostMapping(value = "/account", produces = "application/json")
	public String account(String userId, int money) {
		LOGGER.info("Account Service ... xid: " + RootContext.getXID());

		if (random.nextBoolean()) {
			throw new RuntimeException("this is a mock Exception");
		}

		int result = jdbcTemplate.update(
				"update account_tbl set money = money - ? where user_id = ?",
				new Object[] { money, userId });
		LOGGER.info("Account Service End ... ");
		if (result == 1) {
			return SUCCESS;
		}
		return FAIL;
	}

}
