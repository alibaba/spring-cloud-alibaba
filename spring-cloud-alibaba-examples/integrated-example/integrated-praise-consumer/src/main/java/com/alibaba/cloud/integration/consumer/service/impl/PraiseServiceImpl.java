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

package com.alibaba.cloud.integration.consumer.service.impl;

import java.sql.Timestamp;

import com.alibaba.cloud.integration.consumer.mapper.PraiseMapper;
import com.alibaba.cloud.integration.consumer.service.PraiseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author TrevorLink
 */
@Service
public class PraiseServiceImpl implements PraiseService {

	@Autowired
	private PraiseMapper praiseMapper;

	@Override
	public void praiseItem(Integer itemId) {
		Timestamp updateTime = new Timestamp(System.currentTimeMillis());
		praiseMapper.praiseItem(itemId, updateTime);
	}

	@Override
	public int getPraise(Integer itemId) {
		return praiseMapper.getPraise(itemId);
	}

}
