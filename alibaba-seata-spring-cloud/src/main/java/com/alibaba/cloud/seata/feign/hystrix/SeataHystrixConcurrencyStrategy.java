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
package com.alibaba.cloud.seata.feign.hystrix;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import io.seata.core.context.RootContext;

import java.util.concurrent.Callable;

/**
 * @author xiaojing
 */
public class SeataHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

	private HystrixConcurrencyStrategy delegate;

	public SeataHystrixConcurrencyStrategy() {
		this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
		HystrixPlugins.reset();
		HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
	}

	@Override
	public <K> Callable<K> wrapCallable(Callable<K> c) {
		if (c instanceof SeataContextCallable) {
			return c;
		}

		Callable<K> wrappedCallable;
		if (this.delegate != null) {
			wrappedCallable = this.delegate.wrapCallable(c);
		}
		else {
			wrappedCallable = c;
		}

		return new SeataContextCallable<>(wrappedCallable);
	}

	private static class SeataContextCallable<K> implements Callable<K> {

		private final Callable<K> actual;
		private final String xid;

		SeataContextCallable(Callable<K> actual) {
			this.actual = actual;
			this.xid = RootContext.getXID();
		}

		@Override
		public K call() throws Exception {
			try {
				RootContext.bind(xid);
				return actual.call();
			}
			finally {
				RootContext.unbind();
			}
		}

	}
}
