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
