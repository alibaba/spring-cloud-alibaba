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

package com.alibaba.cloud.seata.feign;

import java.util.ArrayList;
import java.util.List;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;

import org.springframework.util.StringUtils;

/**
 * @author wang.liang
 */
public class SeataFeignRequestInterceptor implements RequestInterceptor {

	@Override
	public void apply(RequestTemplate template) {
		String xid = RootContext.getXID();
		if (!StringUtils.hasLength(xid)) {
			return;
		}

		List<String> seataXid = new ArrayList<>();
		seataXid.add(xid);
		template.header(RootContext.KEY_XID, xid);
	}
}
