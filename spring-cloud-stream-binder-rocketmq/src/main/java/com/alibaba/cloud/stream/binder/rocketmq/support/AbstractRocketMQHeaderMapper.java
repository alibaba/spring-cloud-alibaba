package com.alibaba.cloud.stream.binder.rocketmq.support;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.rocketmq.common.message.MessageConst;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.Assert;

/**
 * Base for RocketMQ header mappers.
 *
 * @author caotc
 * @since 2.1.1.RELEASE
 */
public abstract class AbstractRocketMQHeaderMapper implements RocketMQHeaderMapper {
	private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	private Charset charset;

	public AbstractRocketMQHeaderMapper() {
		this(DEFAULT_CHARSET);
	}

	public AbstractRocketMQHeaderMapper(Charset charset) {
		Assert.notNull(charset, "'charset' cannot be null");
		this.charset = charset;
	}

	protected boolean matches(String headerName) {
		return !MessageConst.STRING_HASH_SET.contains(headerName)
				&& !MessageHeaders.ID.equals(headerName)
				&& !MessageHeaders.TIMESTAMP.equals(headerName)
				&& !MessageHeaders.CONTENT_TYPE.equals(headerName)
				&& !MessageHeaders.REPLY_CHANNEL.equals(headerName)
				&& !MessageHeaders.ERROR_CHANNEL.equals(headerName);
	}

	public Charset getCharset() {
		return charset;
	}

	public void setCharset(Charset charset) {
		Assert.notNull(charset, "'charset' cannot be null");
		this.charset = charset;
	}
}
