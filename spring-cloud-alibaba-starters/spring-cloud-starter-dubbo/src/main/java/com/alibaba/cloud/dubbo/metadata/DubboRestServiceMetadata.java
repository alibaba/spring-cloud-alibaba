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

/**
 * Dubbo Rest Service Metadata.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboRestServiceMetadata {

	private final ServiceRestMetadata SERVICE_REST_METADATA;

	private final RestMethodMetadata REST_METHOD_METADATA;

	public DubboRestServiceMetadata(ServiceRestMetadata serviceRestMetadata,
			RestMethodMetadata restMethodMetadata) {
		this.SERVICE_REST_METADATA = serviceRestMetadata;
		this.REST_METHOD_METADATA = restMethodMetadata;
	}

	public ServiceRestMetadata getServiceRestMetadata() {
		return SERVICE_REST_METADATA;
	}

	public RestMethodMetadata getRestMethodMetadata() {
		return REST_METHOD_METADATA;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DubboRestServiceMetadata)) {
			return false;
		}
		DubboRestServiceMetadata that = (DubboRestServiceMetadata) o;
		return Objects.equals(SERVICE_REST_METADATA, that.SERVICE_REST_METADATA)
				&& Objects.equals(REST_METHOD_METADATA, that.REST_METHOD_METADATA);
	}

	@Override
	public int hashCode() {
		return Objects.hash(SERVICE_REST_METADATA, REST_METHOD_METADATA);
	}

}
