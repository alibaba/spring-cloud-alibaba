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

package com.alibaba.cloud.routing.decorator;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import feign.FeignException;
import feign.Response;
import feign.Util;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.stereotype.Component;

/**
 * @author xqw
 * @author 550588941@qq.com
 */

@Component
public class OutlierDetectionFeignResponseDecoder extends SpringDecoder {

	public OutlierDetectionFeignResponseDecoder(
			ObjectFactory<HttpMessageConverters> messageConverters) {

		super(messageConverters);
	}

	@Override
	public Object decode(Response response, Type type)
			throws IOException, FeignException {

		Reader reader = response.body().asReader(StandardCharsets.UTF_8);
		if (Objects.isNull(Util.toString(reader))) {

			return super.decode(
					response.toBuilder()
							.body(Util.toString(reader), StandardCharsets.UTF_8).build(),
					type);
		}

		return Util.toString(reader);

	}

}
