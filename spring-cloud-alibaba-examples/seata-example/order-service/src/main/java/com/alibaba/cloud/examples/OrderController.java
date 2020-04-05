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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */
@RestController
public class OrderController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

	private static final String SUCCESS = "SUCCESS";

	private static final String FAIL = "FAIL";

	private static final String USER_ID = "U100001";

	private static final String COMMODITY_CODE = "C00321";

	private final JdbcTemplate jdbcTemplate;

	private final RestTemplate restTemplate;

	private Random random;

	public OrderController(JdbcTemplate jdbcTemplate, RestTemplate restTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.restTemplate = restTemplate;
		this.random = new Random();
	}

	@PostMapping(value = "/order", produces = "application/json")
	public String order(String userId, String commodityCode, int orderCount) {
		LOGGER.info("Order Service Begin ... xid: " + RootContext.getXID());

		int orderMoney = calculate(commodityCode, orderCount);

		invokerAccountService(orderMoney);

		final Order order = new Order();
		order.userId = userId;
		order.commodityCode = commodityCode;
		order.count = orderCount;
		order.money = orderMoney;

		KeyHolder keyHolder = new GeneratedKeyHolder();

		int result = jdbcTemplate.update(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				PreparedStatement pst = con.prepareStatement(
						"insert into order_tbl (user_id, commodity_code, count, money) values (?, ?, ?, ?)",
						PreparedStatement.RETURN_GENERATED_KEYS);
				pst.setObject(1, order.userId);
				pst.setObject(2, order.commodityCode);
				pst.setObject(3, order.count);
				pst.setObject(4, order.money);
				return pst;
			}
		}, keyHolder);

		order.id = keyHolder.getKey().longValue();

		if (random.nextBoolean()) {
			throw new RuntimeException("this is a mock Exception");
		}

		LOGGER.info("Order Service End ... Created " + order);

		if (result == 1) {
			return SUCCESS;
		}
		return FAIL;
	}

	private int calculate(String commodityId, int orderCount) {
		return 2 * orderCount;
	}

	private void invokerAccountService(int orderMoney) {
		String url = "http://127.0.0.1:18084/account";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();

		map.add("userId", USER_ID);
		map.add("money", orderMoney + "");

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(
				map, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, request,
				String.class);
	}

}
