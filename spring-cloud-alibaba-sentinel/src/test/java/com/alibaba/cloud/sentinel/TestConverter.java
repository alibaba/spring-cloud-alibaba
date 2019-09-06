/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel;

import java.io.IOException;
import java.util.List;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class TestConverter implements Converter<String, List<ParamFlowRule>> {
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public List<ParamFlowRule> convert(String s) {
		try {
			return objectMapper.readValue(s, new TypeReference<List<ParamFlowRule>>() {
			});
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
