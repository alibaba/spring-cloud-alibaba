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

package com.alibaba.cloud.consumer.feign.decorator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.consumer.feign.constant.FeignConsumerConstants;
import com.alibaba.cloud.consumer.feign.entity.NodeInfo;
import feign.Client;
import feign.Request;
import feign.Response;

/**
 * @author yuluo
 * @author 1481556636@qq.com Rewrite the feign client to get the feign response.
 */

public class FeignClientDecorator implements Client {

	private final Client delegate;

	public FeignClientDecorator(Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {

		Response response = delegate.execute(request, options);

		List<Map<String, List<String>>> labelList = new ArrayList<>();

		request.headers().forEach((k, v) -> {
			Map<String, List<String>> map = new HashMap<>();
			List<String> headerValue = new ArrayList<>(v);
			map.put(k, headerValue);
			labelList.add(map);
		});
		NodeInfo.set(FeignConsumerConstants.APPLICATION_NAME, labelList);

		return response;
	}

}
