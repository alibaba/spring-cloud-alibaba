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
		 * 访问成功.
		 */
		_200(200, "访问成功"),
		/**
		 * 收数据的数据类型不匹配.
		 */
		_400(400, "收数据的数据类型不匹配"),
		/**
		 * 服务器拒绝请求.
		 */
		_403(403, "服务器拒绝请求"),
		/**
		 * 服务器找不到请求的网页，输入链接有误.
		 */
		_404(404, "服务器找不到请求的网页，输入链接有误"),
		/**
		 * 服务器遇到错误，无法完成请求.
		 */
		_500(500, "服务器遇到错误，无法完成请求"),
		/**
		 * 服务器作为网关或代理，从上游服务器收到无效响应.
		 */
		_502(502, "服务器作为网关或代理，从上游服务器收到无效响应");

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
