package org.springframework.cloud.alibaba.sentinel.feign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import feign.hystrix.FallbackFactory;

/**
 * @author lengleng
 * <p>
 * sentinel auto fallback，also cglib unified return value
 */
public final class SentinelFeginAutoFallbackFactory<T> implements FallbackFactory<T> {
	private static final Logger logger = LoggerFactory
			.getLogger(SentinelFeginAutoFallbackFactory.class);
	static final SentinelFeginAutoFallbackFactory INSTANCE = new SentinelFeginAutoFallbackFactory();

	@SuppressWarnings("unchecked")
	T create(final Class<?> type, final Throwable cause) {
		logger.error("Fallback class:[{}] message:[{}]", type.getName(),
				cause.getMessage());
		return null;
	}

	@Override
	public T create(Throwable cause) {
		logger.error("Fallback message:[{}]", cause.getMessage());
		return null;
	}
}
