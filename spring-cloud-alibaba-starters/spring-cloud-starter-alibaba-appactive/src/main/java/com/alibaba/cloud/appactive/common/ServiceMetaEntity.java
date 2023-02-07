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

package com.alibaba.cloud.appactive.common;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class ServiceMetaEntity implements Comparable<ServiceMetaEntity> {

	private String uriPrefix;

	private String ra;

	public ServiceMetaEntity() {
	}

	public ServiceMetaEntity(String uriPrefix, String ra) {
		this.uriPrefix = uriPrefix;
		this.ra = ra;
	}

	public String getUriPrefix() {
		return uriPrefix;
	}

	public void setUriPrefix(String uriPrefix) {
		this.uriPrefix = uriPrefix;
	}

	public String getRa() {
		return ra;
	}

	public void setRa(String ra) {
		this.ra = ra;
	}

	@Override
	public String toString() {
		return "ServiceMeta{" + "uriPrefix='" + uriPrefix + '\'' + ", ra=" + ra + '}';
	}

	@Override
	public int compareTo(ServiceMetaEntity o) {
		int pre = this.uriPrefix.compareTo(o.getUriPrefix());
		return pre == 0 ? this.ra.compareTo(o.getRa()) : pre;
	}

}
