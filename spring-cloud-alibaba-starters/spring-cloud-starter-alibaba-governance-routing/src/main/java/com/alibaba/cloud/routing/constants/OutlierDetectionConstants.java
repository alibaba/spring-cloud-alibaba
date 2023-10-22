/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.constants;

/**
 * @author xqw
 */
public final class OutlierDetectionConstants {

	private OutlierDetectionConstants() {
	}

	/**
	 * Instance error called max.
	 */
	public static final int instanceErrorCalledMax = 5;

	/**
	 * Response Code.
	 */
	public enum ResponseCode {

		/**
		 * Access successful.
		 */
		_200(200, "Access successful"),
		/**
		 * The data types do not match.
		 */
		_400(400, "The data types do not match"),
		/**
		 * The server rejected the request.
		 */
		_403(403, "The server rejected the request"),
		/**
		 * The server could not find the requested web page and entered the link incorrectly.
		 */
		_404(404, "The server could not find the requested web page and entered the link incorrectly"),
		/**
		 * The server encountered an error and could not complete the request.
		 */
		_500(500, "The server encountered an error and could not complete the request"),
		/**
		 * The server, acting as a gateway or proxy, receives an invalid response from the upstream server.
		 */
		_502(502, "The server, acting as a gateway or proxy, receives an invalid response from the upstream server");

		/**
		 * Code.
		 */
		private Integer code;

		/**
		 * info.
		 */
		private String info;

		ResponseCode(Integer code, String info) {
			this.code = code;
			this.info = info;
		}

		public Integer getCode() {
			return code;
		}

		public String getInfo() {
			return info;
		}

	}

}
