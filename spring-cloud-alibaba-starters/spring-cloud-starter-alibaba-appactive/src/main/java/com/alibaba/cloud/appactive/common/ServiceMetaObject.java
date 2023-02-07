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

import java.util.List;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class ServiceMetaObject {

	private List<ServiceMetaEntity> serviceMetaList;

	/**
	 * string of serviceMetaList.
	 */
	private String meta;

	private String md5OfList;

	public ServiceMetaObject(List<ServiceMetaEntity> serviceMetaList, String md5OfList) {
		this.serviceMetaList = serviceMetaList;
		this.md5OfList = md5OfList;
	}

	public ServiceMetaObject() {
	}

	public List<ServiceMetaEntity> getServiceMetaList() {
		return serviceMetaList;
	}

	public void setServiceMetaList(List<ServiceMetaEntity> serviceMetaList) {
		this.serviceMetaList = serviceMetaList;
	}

	public String getMd5OfList() {
		return md5OfList;
	}

	public void setMd5OfList(String md5OfList) {
		this.md5OfList = md5OfList;
	}

	public String getMeta() {
		return meta;
	}

	public void setMeta(String meta) {
		this.meta = meta;
	}

	@Override
	public String toString() {
		return "ServiceMetaObject{" + "ServiceMetaEntityList=" + serviceMetaList
				+ ", md5OfList='" + md5OfList + '\'' + '}';
	}

}
