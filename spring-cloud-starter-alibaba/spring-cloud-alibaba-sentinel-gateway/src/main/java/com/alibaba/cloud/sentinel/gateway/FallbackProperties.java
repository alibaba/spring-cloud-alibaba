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

package com.alibaba.cloud.sentinel.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FallbackProperties {

	/**
	 * The fallback mode for sentinel spring-cloud-gateway. choose `redirect` or
	 * `response`.
	 */
	private String mode;

	/**
	 * Redirect Url for `redirect` mode.
	 */
	private String redirect;

	/**
	 * Response Body for `response` mode.
	 */
	private String responseBody;

	/**
	 * Response Status for `response` mode.
	 */
	private Integer responseStatus = HttpStatus.TOO_MANY_REQUESTS.value();

	/**
	 * Content-Type for `response` mode.
	 */
	private String contentType = MediaType.APPLICATION_JSON.toString();

	public String getMode() {
		return mode;
	}

	public FallbackProperties setMode(String mode) {
		this.mode = mode;
		return this;
	}

	public String getRedirect() {
		return redirect;
	}

	public FallbackProperties setRedirect(String redirect) {
		this.redirect = redirect;
		return this;
	}

	public String getResponseBody() {
		return responseBody;
	}

	public FallbackProperties setResponseBody(String responseBody) {
		this.responseBody = responseBody;
		return this;
	}

	public Integer getResponseStatus() {
		return responseStatus;
	}

	public FallbackProperties setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
		return this;
	}

	public String getContentType() {
		return contentType;
	}

	public FallbackProperties setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

}
