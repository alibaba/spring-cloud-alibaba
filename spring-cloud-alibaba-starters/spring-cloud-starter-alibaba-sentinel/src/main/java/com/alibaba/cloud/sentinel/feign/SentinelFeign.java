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

package com.alibaba.cloud.sentinel.feign;

import feign.Contract;
import feign.Feign;
import feign.InvocationHandlerFactory;
import feign.hystrix.HystrixFeign;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * {@link Feign.Builder} like {@link HystrixFeign.Builder}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public final class SentinelFeign {

	private SentinelFeign() {

	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder extends Feign.Builder implements ApplicationContextAware {

		private Contract contract = new Contract.Default();

		private ApplicationContext applicationContext;

		@Override
		public Feign.Builder invocationHandlerFactory(
				InvocationHandlerFactory invocationHandlerFactory) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Builder contract(Contract contract) {
			this.contract = contract;
			return this;
		}

		@Override
		public Feign build() {
			super.invocationHandlerFactory(buildInvocationHandlerFactory());
			super.contract(buildContract());
			return super.build();
		}

		protected SentinelContractHolder buildContract() {
			return new SentinelContractHolder(contract);
		}

		protected InvocationHandlerFactory buildInvocationHandlerFactory() {
			return new SentinelInvocationHandlerFactory(this.applicationContext);
		}

		@Override
		public void setApplicationContext(@NonNull ApplicationContext applicationContext)
				throws BeansException {
			this.applicationContext = applicationContext;
		}

	}

}
