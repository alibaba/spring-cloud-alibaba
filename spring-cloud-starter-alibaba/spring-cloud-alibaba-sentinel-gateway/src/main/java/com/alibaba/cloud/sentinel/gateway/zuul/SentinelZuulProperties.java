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

package com.alibaba.cloud.sentinel.gateway.zuul;

import com.alibaba.cloud.sentinel.gateway.ConfigConstants;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulErrorFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPostFilter;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@ConfigurationProperties(prefix = ConfigConstants.ZUUl_PREFIX)
public class SentinelZuulProperties {

	@NestedConfigurationProperty
	private SentinelZuulProperties.Order order = new SentinelZuulProperties.Order();

	public Order getOrder() {
		return order;
	}

	public SentinelZuulProperties setOrder(Order order) {
		this.order = order;
		return this;
	}

	public static class Order {

		/**
		 * The order of {@link SentinelZuulPreFilter}.
		 */
		private int pre = 10000;

		/**
		 * The order of {@link SentinelZuulPostFilter}.
		 */
		private int post = ZuulConstant.SEND_RESPONSE_FILTER_ORDER;

		/**
		 * The order of {@link SentinelZuulErrorFilter}.
		 */
		private int error = -1;

		public int getPre() {
			return pre;
		}

		public void setPre(int pre) {
			this.pre = pre;
		}

		public int getPost() {
			return post;
		}

		public void setPost(int post) {
			this.post = post;
		}

		public int getError() {
			return error;
		}

		public void setError(int error) {
			this.error = error;
		}

	}

}
