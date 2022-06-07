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

package com.alibaba.cloud.examples.api.impl;

import com.alibaba.cloud.examples.api.StorageDubboService;
import io.seata.core.context.RootContext;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:zhangqian9158@gmail.com">ZHANGQIAN</a>
 */

@DubboService
public class StorageDubboServiceImpl implements StorageDubboService {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(StorageDubboServiceImpl.class);

	private static final String SUCCESS = "SUCCESS";

	private static final String FAIL = "FAIL";

	private final JdbcTemplate jdbcTemplate;

	public StorageDubboServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	@Transactional
	public String updateStorage(String commodityCode, int count) {
		LOGGER.info("Dubbo Storage Service Begin ... xid: " + RootContext.getXID());
		int result = jdbcTemplate.update(
				"update storage_tbl set count = count - ? where commodity_code = ?",
				new Object[] { count, commodityCode });
		LOGGER.info("Storage Service End ... ");
		if (result == 1) {
			return SUCCESS;
		}
		return FAIL;
	}

}
