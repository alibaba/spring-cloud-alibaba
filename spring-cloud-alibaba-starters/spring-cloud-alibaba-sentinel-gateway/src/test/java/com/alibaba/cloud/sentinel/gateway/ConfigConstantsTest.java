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

package com.alibaba.cloud.sentinel.gateway;

import junit.framework.Assert;
import org.junit.Test;

public class ConfigConstantsTest {
	@Test
	public void testConfigConstants() {
		Assert.assertEquals("11", ConfigConstants.APP_TYPE_SCG_GATEWAY);
		Assert.assertEquals("spring.cloud.sentinel.scg", ConfigConstants.GATEWAY_PREFIX);
		Assert.assertEquals("response", ConfigConstants.FALLBACK_MSG_RESPONSE);
		Assert.assertEquals("redirect", ConfigConstants.FALLBACK_REDIRECT);
	}
}
