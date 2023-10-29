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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.cloud.routing.cache.GlobalInstanceStatusListCache;
import com.alibaba.cloud.routing.constants.OutlierDetectionConstants;
import com.alibaba.cloud.routing.model.ServiceInstanceInfo;
import feign.Client;
import feign.Request;
import feign.Response;

/**
 * @author xqw
 * @author 550588941@qq.com
 */

public class OutlierDetectionFeignClientDecorator implements Client {

	private final Client delegate;

	public OutlierDetectionFeignClientDecorator(Client delegate) {
		this.delegate = delegate;
	}

	@Override
	public Response execute(Request request, Request.Options options) throws IOException {

		Response response = delegate.execute(request, options);

		parseResponse(request, response);

		return response;
	}

	private void parseResponse(Request request, Response response)
			throws MalformedURLException {

		URL url = new URL(request.url());
		String instanceName = url.getHost() + ":" + url.getPort();
		ServiceInstanceInfo sif = GlobalInstanceStatusListCache
				.getInstanceByInstanceName(instanceName);

		// The use of metrics is still under investigation.
		// FastCompass counter = MetricManager.getFastCompass("sca-instance"
		// , MetricName.build(instanceName + ".counter"));
		// long start = System.currentTimeMillis();

		if (response.status() == OutlierDetectionConstants.ResponseCode._500.getCode()) {
			// long duration = System.currentTimeMillis() - start;
			// counter.record(duration,"error");
			// sif.setCompass(counter);
			AtomicInteger consecutiveErrors = sif.getConsecutiveErrors();
			if (Objects.isNull(consecutiveErrors)) {
				consecutiveErrors = new AtomicInteger(1);
			} else {
				int andIncrement = sif.getConsecutiveErrors().get();
				andIncrement ++;
				consecutiveErrors = new AtomicInteger(andIncrement);
			}
			sif.setConsecutiveErrors(consecutiveErrors);
			sif.setRemoveTime(System.currentTimeMillis());
			System.err.println("设置服务错误次数之后的全局缓存数据：" + GlobalInstanceStatusListCache.getAll());
		}
		else {
			// long duration = System.currentTimeMillis() - start;
			// counter.record(duration, "success");
			// sif.setCompass(counter);
		}

	}

}
