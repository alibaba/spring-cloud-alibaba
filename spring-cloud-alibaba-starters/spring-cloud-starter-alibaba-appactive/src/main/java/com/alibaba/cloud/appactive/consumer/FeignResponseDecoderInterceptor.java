/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.appactive.consumer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

import com.alibaba.cloud.appactive.common.UriContext;
import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class FeignResponseDecoderInterceptor implements Decoder {

	private static final Logger logger = LogUtil.getLogger();

	final Decoder delegate;

	public FeignResponseDecoderInterceptor(Decoder delegate) {
		Objects.requireNonNull(delegate, "Decoder must not be null. ");
		this.delegate = delegate;
	}

	@Override
	public Object decode(Response response, Type type)
			throws IOException, FeignException {
		Object object = delegate.decode(response, type);
		logger.info("FeignResponseDecoderInterceptor uri {} for request {} got cleared by {}",
				UriContext.getUriPath(), response.request().url(), delegate.getClass());
		UriContext.clearContext();
		return object;
	}

}
