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

package com.alibaba.cloud.dubbo.metadata;

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Service Rest Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RestMethodMetadata
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceRestMetadata {

	private String url;

	private Set<RestMethodMetadata> meta;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<RestMethodMetadata> getMeta() {
		return meta;
	}

	public void setMeta(Set<RestMethodMetadata> meta) {
		this.meta = meta;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ServiceRestMetadata)) {
			return false;
		}
		ServiceRestMetadata that = (ServiceRestMetadata) o;
		return Objects.equals(url, that.url) && Objects.equals(meta, that.meta);
	}

	@Override
	public int hashCode() {
		return Objects.hash(url, meta);
	}

}
