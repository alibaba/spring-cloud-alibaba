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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateGroupItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
public class SentinelGatewayAutoConfiguration {

	@ConditionalOnClass(ObjectMapper.class)
	@Configuration(proxyBeanMethods = false)
	protected static class SentinelConverterConfiguration {

		static class ApiPredicateItemDeserializer
				extends StdDeserializer<ApiPredicateItem> {

			private Map<String, Class<? extends ApiPredicateItem>> registry = new HashMap<String, Class<? extends ApiPredicateItem>>();

			ApiPredicateItemDeserializer() {
				super(ApiPredicateItem.class);
			}

			void registerApiPredicateItem(String uniqueAttribute,
					Class<? extends ApiPredicateItem> apiPredicateItemClass) {
				registry.put(uniqueAttribute, apiPredicateItemClass);
			}

			@Override
			public ApiPredicateItem deserialize(JsonParser jp,
					DeserializationContext ctxt) throws IOException {
				ObjectMapper mapper = (ObjectMapper) jp.getCodec();
				ObjectNode root = mapper.readTree(jp);
				Class<? extends ApiPredicateItem> apiPredicateItemClass = null;
				Iterator<Entry<String, JsonNode>> elementsIterator = root.fields();
				while (elementsIterator.hasNext()) {
					Entry<String, JsonNode> element = elementsIterator.next();
					String name = element.getKey();
					if (registry.containsKey(name)) {
						apiPredicateItemClass = registry.get(name);
						break;
					}
				}
				if (apiPredicateItemClass == null) {
					return null;
				}
				return mapper.readValue(root.toString(), apiPredicateItemClass);
			}

		}

		@Configuration(proxyBeanMethods = false)
		protected static class SentinelJsonConfiguration {

			private ObjectMapper objectMapper = new ObjectMapper();

			public SentinelJsonConfiguration() {
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);

				ApiPredicateItemDeserializer deserializer = new ApiPredicateItemDeserializer();
				deserializer.registerApiPredicateItem("pattern",
						ApiPathPredicateItem.class);
				deserializer.registerApiPredicateItem("items",
						ApiPredicateGroupItem.class);
				SimpleModule module = new SimpleModule(
						"PolymorphicApiPredicateItemDeserializerModule",
						new Version(1, 0, 0, null));
				module.addDeserializer(ApiPredicateItem.class, deserializer);
				objectMapper.registerModule(module);
			}

			@Bean("sentinel-json-gw-flow-converter")
			public JsonConverter jsonGatewayFlowConverter() {
				return new JsonConverter(objectMapper, GatewayFlowRule.class);
			}

			@Bean("sentinel-json-gw-api-group-converter")
			public JsonConverter jsonApiConverter() {
				return new JsonConverter(objectMapper, ApiDefinition.class);
			}

		}

		@ConditionalOnClass(XmlMapper.class)
		@Configuration(proxyBeanMethods = false)
		protected static class SentinelXmlConfiguration {

			private XmlMapper xmlMapper = new XmlMapper();

			public SentinelXmlConfiguration() {
				xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
				ApiPredicateItemDeserializer deserializer = new ApiPredicateItemDeserializer();
				deserializer.registerApiPredicateItem("pattern",
						ApiPathPredicateItem.class);
				deserializer.registerApiPredicateItem("items",
						ApiPredicateGroupItem.class);
				SimpleModule module = new SimpleModule(
						"PolymorphicGatewayDeserializerModule",
						new Version(1, 0, 0, null));
				module.addDeserializer(ApiPredicateItem.class, deserializer);
				xmlMapper.registerModule(module);
			}

			@Bean("sentinel-xml-gw-flow-converter")
			public XmlConverter xmlGatewayFlowConverter() {
				return new XmlConverter(xmlMapper, GatewayFlowRule.class);
			}

			@Bean("sentinel-xml-gw-api-group-converter")
			public XmlConverter xmlApiConverter() {
				return new XmlConverter(xmlMapper, ApiDefinition.class);
			}

		}

	}

}
