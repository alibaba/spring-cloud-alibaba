/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.integration.order.service.impl;

import java.sql.Timestamp;

import com.alibaba.cloud.integration.common.BusinessException;
import com.alibaba.cloud.integration.common.Result;
import com.alibaba.cloud.integration.order.entity.Order;
import com.alibaba.cloud.integration.order.feign.AccountServiceFeignClient;
import com.alibaba.cloud.integration.order.feign.StorageServiceFeignClient;
import com.alibaba.cloud.integration.order.feign.dto.AccountDTO;
import com.alibaba.cloud.integration.order.feign.dto.StorageDTO;
import com.alibaba.cloud.integration.order.mapper.OrderMapper;
import com.alibaba.cloud.integration.order.service.OrderService;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.alibaba.cloud.integration.common.ResultEnum.COMMON_FAILED;

/**
 * @author TrevorLink
 */
@Service
public class OrderServiceImpl implements OrderService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private OrderMapper orderMapper;

	@Autowired
	private AccountServiceFeignClient accountService;

	@Autowired
	private StorageServiceFeignClient storageService;

	@Override
	@GlobalTransactional
	public Result<?> createOrder(String userId, String commodityCode, Integer count) {

		logger.info("[createOrder] current XID: {}", RootContext.getXID());

		// deduct storage
		StorageDTO storageDTO = new StorageDTO();
		storageDTO.setCommodityCode(commodityCode);
		storageDTO.setCount(count);
		Integer storageCode = storageService.reduceStock(storageDTO).getCode();
		if (storageCode.equals(COMMON_FAILED.getCode())) {
			throw new BusinessException("stock not enough");
		}

		// deduct balance
		int price = count * 2;
		AccountDTO accountDTO = new AccountDTO();
		accountDTO.setUserId(userId);
		accountDTO.setPrice(price);
		Integer accountCode = accountService.reduceBalance(accountDTO).getCode();
		if (accountCode.equals(COMMON_FAILED.getCode())) {
			throw new BusinessException("balance not enough");
		}

		// save order
		Order order = new Order();
		order.setUserId(userId);
		order.setCommodityCode(commodityCode);
		order.setCount(count);
		order.setMoney(price);
		order.setCreateTime(new Timestamp(System.currentTimeMillis()));
		order.setUpdateTime(new Timestamp(System.currentTimeMillis()));
		orderMapper.saveOrder(order);
		logger.info("[createOrder] orderId: {}", order.getId());

		return Result.success(order);
	}

}
