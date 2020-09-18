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

package com.alibaba.cloud.seata.feign.hystrix;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariable;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableLifecycle;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.hystrix.strategy.properties.HystrixProperty;
import io.seata.core.context.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * @author xiaojing
 */
public class SeataHystrixConcurrencyStrategy extends HystrixConcurrencyStrategy {

	private final Logger logger = LoggerFactory
			.getLogger(SeataHystrixConcurrencyStrategy.class);

	private HystrixConcurrencyStrategy delegate;

	public SeataHystrixConcurrencyStrategy() {
		try {
			this.delegate = HystrixPlugins.getInstance().getConcurrencyStrategy();
			if (this.delegate instanceof SeataHystrixConcurrencyStrategy) {
				return;
			}
			HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins
					.getInstance().getCommandExecutionHook();
			HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance()
					.getEventNotifier();
			HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance()
					.getMetricsPublisher();
			HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance()
					.getPropertiesStrategy();
			logCurrentStateOfHystrixPlugins(eventNotifier, metricsPublisher,
					propertiesStrategy);
			HystrixPlugins.reset();
			HystrixPlugins.getInstance().registerConcurrencyStrategy(this);
			HystrixPlugins.getInstance()
					.registerCommandExecutionHook(commandExecutionHook);
			HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
			HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
			HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
		}
		catch (Exception ex) {
			logger.error("Failed to register Seata Hystrix Concurrency Strategy", ex);
		}
	}

	private void logCurrentStateOfHystrixPlugins(HystrixEventNotifier eventNotifier,
			HystrixMetricsPublisher metricsPublisher,
			HystrixPropertiesStrategy propertiesStrategy) {
		if (logger.isDebugEnabled()) {
			logger.debug("Current Hystrix plugins configuration is ["
					+ "concurrencyStrategy [" + this.delegate + "]," + "eventNotifier ["
					+ eventNotifier + "]," + "metricPublisher [" + metricsPublisher + "],"
					+ "propertiesStrategy [" + propertiesStrategy + "]," + "]");
			logger.debug("Registering Seata Hystrix Concurrency Strategy.");
		}
	}

	@Override
	public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
			HystrixProperty<Integer> corePoolSize,
			HystrixProperty<Integer> maximumPoolSize,
			HystrixProperty<Integer> keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		return this.delegate.getThreadPool(threadPoolKey, corePoolSize, maximumPoolSize,
				keepAliveTime, unit, workQueue);
	}

	@Override
	public ThreadPoolExecutor getThreadPool(HystrixThreadPoolKey threadPoolKey,
			HystrixThreadPoolProperties threadPoolProperties) {
		return this.delegate.getThreadPool(threadPoolKey, threadPoolProperties);
	}

	@Override
	public BlockingQueue<Runnable> getBlockingQueue(int maxQueueSize) {
		return this.delegate.getBlockingQueue(maxQueueSize);
	}

	@Override
	public <T> HystrixRequestVariable<T> getRequestVariable(
			HystrixRequestVariableLifecycle<T> rv) {
		return this.delegate.getRequestVariable(rv);
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
		if (wrappedCallable instanceof SeataContextCallable) {
			return wrappedCallable;
		}

		return new SeataContextCallable<>(wrappedCallable,
				RequestContextHolder.getRequestAttributes());
	}

	private static class SeataContextCallable<K> implements Callable<K> {

		private final Callable<K> actual;

		private final String xid;

		private final RequestAttributes requestAttributes;

		SeataContextCallable(Callable<K> actual, RequestAttributes requestAttribute) {
			this.actual = actual;
			this.requestAttributes = requestAttribute;
			this.xid = RootContext.getXID();
		}

		@Override
		public K call() throws Exception {
			try {
				RequestContextHolder.setRequestAttributes(requestAttributes);
				RootContext.bind(xid);
				return actual.call();
			}
			finally {
				RootContext.unbind();
				RequestContextHolder.resetRequestAttributes();
			}
		}

	}

}
