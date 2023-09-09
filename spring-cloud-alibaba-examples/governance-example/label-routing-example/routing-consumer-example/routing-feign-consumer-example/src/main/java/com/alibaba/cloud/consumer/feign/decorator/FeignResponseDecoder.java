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
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import com.alibaba.cloud.consumer.feign.entity.NodeInfo;
import feign.FeignException;
import feign.Response;
import feign.Util;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Component
public class FeignResponseDecoder extends SpringDecoder {

	@Resource
	private DiscoveryClient discoveryClient;

	public FeignResponseDecoder(ObjectFactory<HttpMessageConverters> messageConverters) {

		super(messageConverters);
	}

	@Override
	public Object decode(Response response, Type type)
			throws IOException, FeignException {

		Reader reader = response.body().asReader(StandardCharsets.UTF_8);
		String json = Util.toString(reader);
		String result = getResult(json);
		if (Objects.isNull(result)) {

			return super.decode(
					response.toBuilder().body(json, StandardCharsets.UTF_8).build(),
					type);
		}

		return result;

	}

	private String getResult(String json) {

		String serverPort = json.substring(21, 26);

		List<String> services = discoveryClient.getServices();
		for (String service : services) {
			List<ServiceInstance> instances = discoveryClient.getInstances(service);
			for (ServiceInstance instance : instances) {
				if ((instance.getPort() + "").equals(serverPort)) {
					String server = instance.getServiceId();
					Map<String, String> metadata = instance.getMetadata();
					List<Map<String, List<String>>> metaList = new ArrayList<>();
					Map<String, List<String>> map = new HashMap<>();
					for (String s : metadata.keySet()) {
						map.put(s, Collections.singletonList(metadata.get(s)));
					}
					map.put("port", Collections.singletonList(instance.getPort() + ""));
					map.put("host", Collections.singletonList(instance.getHost()));
					map.put("instanceId",
							Collections.singletonList(instance.getInstanceId()));
					metaList.add(map);

					NodeInfo.set(server, metaList);
				}
			}
		}

		return json;
	}

}
