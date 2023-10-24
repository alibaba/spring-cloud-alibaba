/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.context;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

public class LabelRoutingContext {

	private static final ThreadLocal<LabelRoutingContext> THREAD_LOCAL = ThreadLocal
			.withInitial(LabelRoutingContext::new);

	private String routingRegion;

	private String routingZone;

	private HttpServletRequest servletRequest;

	public HttpServletRequest getServletRequest() {

		return servletRequest;
	}

	public void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	public static LabelRoutingContext getCurrentContext() {

		return THREAD_LOCAL.get();
	}

	public static void clearCurrentContext() {

		THREAD_LOCAL.remove();
	}

	public String getRoutingRegion() {
		return routingRegion;
	}

	public void setRoutingRegion(String routingRegion) {
		this.routingRegion = routingRegion;
	}

	public String getRoutingZone() {
		return routingZone;
	}

	public void setRoutingZone(String routingZone) {
		this.routingZone = routingZone;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object object) {
		return EqualsBuilder.reflectionEquals(this, object);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

}
