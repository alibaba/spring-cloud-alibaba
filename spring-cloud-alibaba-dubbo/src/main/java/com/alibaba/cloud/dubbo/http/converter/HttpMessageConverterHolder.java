/*
 * Copyright (C) 2018 the original author or authors.
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
package com.alibaba.cloud.dubbo.http.converter;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;

/**
 * {@link HttpMessageConverter} Holder with {@link MediaType}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpMessageConverterHolder {

	private final MediaType mediaType;

	private final HttpMessageConverter<?> converter;

	public HttpMessageConverterHolder(MediaType mediaType,
			HttpMessageConverter<?> converter) {
		this.mediaType = mediaType;
		this.converter = converter;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public HttpMessageConverter<?> getConverter() {
		return converter;
	}
}
