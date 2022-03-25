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

package com.alibaba.cloud.examples.schedulerx.domain;

import java.util.Map;

import com.alibaba.schedulerx.worker.processor.BizSubTask;
import com.google.common.collect.Maps;

/**
 * AccountInfo.
 *
 * @author xiaomeng.hxm
 **/
public class AccountInfo implements BizSubTask {

	private long id;
	private String name;
	private String accountId;

	public AccountInfo(long id, String name, String accountId) {
		this.id = id;
		this.name = name;
		this.accountId = accountId;
	}

	/**
	 * implement labelMap.
	 * @return labelMap
	 */
	@Override
	public Map<String, String> labelMap() {
		Map<String, String> labelMap = Maps.newHashMap();
		labelMap.put("user", name);
		return labelMap;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

}