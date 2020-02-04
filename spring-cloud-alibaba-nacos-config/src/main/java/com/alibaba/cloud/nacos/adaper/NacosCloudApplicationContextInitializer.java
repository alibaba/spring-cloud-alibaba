/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.adaper;

import com.alibaba.boot.nacos.config.autoconfigure.NacosConfigApplicationContextInitializer;
import com.alibaba.boot.nacos.config.autoconfigure.NacosConfigEnvironmentProcessor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosCloudApplicationContextInitializer extends
		NacosConfigApplicationContextInitializer {

	public NacosCloudApplicationContextInitializer(
			NacosConfigEnvironmentProcessor configEnvironmentProcessor) {
		super(configEnvironmentProcessor);
	}

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		NacosApplicationContext applicationContext = new NacosApplicationContext(context);
		super.initialize(applicationContext);
	}
}
