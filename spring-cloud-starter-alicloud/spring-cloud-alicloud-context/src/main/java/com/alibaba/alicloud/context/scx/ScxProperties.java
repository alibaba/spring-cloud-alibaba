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

package com.alibaba.alicloud.context.scx;

import com.alibaba.cloud.context.scx.ScxConfiguration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author xiaolongzuo
 */
@ConfigurationProperties("spring.cloud.alicloud.scx")
public class ScxProperties implements ScxConfiguration {

	/**
	 * Group id, please see <a href=
	 * "https://help.aliyun.com/document_detail/35359.html?spm=a2c4g.11186623.6.721.69ca5763p9IJly">scx
	 * docs</a>.
	 */
	private String groupId;

	/**
	 * Domain name, please see <a href=
	 * "https://help.aliyun.com/document_detail/35359.html?spm=a2c4g.11186623.6.721.69ca5763p9IJly">scx
	 * docs</a>.
	 */
	private String domainName;

	@Override
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	@Override
	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

}
