/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.seata;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaojing
 */
@ConfigurationProperties("spring.cloud.alibaba.seata")
public class SeataProperties {

	// todo support config Seata server information

	/**
	 * Seata tx service group.default is ${spring.application.name}-seata-service-group.
	 */
	private String txServiceGroup;

	public String getTxServiceGroup() {
		return txServiceGroup;
	}

	public void setTxServiceGroup(String txServiceGroup) {
		this.txServiceGroup = txServiceGroup;
	}

}
