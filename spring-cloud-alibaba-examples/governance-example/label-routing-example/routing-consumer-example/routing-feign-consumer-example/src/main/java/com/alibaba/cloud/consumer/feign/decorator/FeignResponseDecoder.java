package com.alibaba.cloud.consumer.feign.decorator;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import javax.annotation.Resource;

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
	public Object decode(Response response, Type type) throws IOException, FeignException {

		Reader reader = response.body().asReader(StandardCharsets.UTF_8);
		String json = Util.toString(reader);
		String result = getResult(json);
		if (Objects.isNull(result)) {

			return super.decode(response.toBuilder().body(json, StandardCharsets.UTF_8).build(), type);
		}

		return result;

	}

	private String getResult(String json) {

		String server = json.substring(9, 26);
		List<ServiceInstance> instances = discoveryClient.getInstances(server);
		System.out.println(instances);

		return json;
	}

}
