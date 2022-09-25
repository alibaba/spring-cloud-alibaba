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

package com.alibaba.cloud.governance.auth.cache;

import com.alibaba.cloud.governance.auth.manager.HeaderRuleManager;
import com.alibaba.cloud.governance.auth.manager.IdentityRuleManager;
import com.alibaba.cloud.governance.auth.manager.IpBlockRuleManager;
import com.alibaba.cloud.governance.auth.manager.JwtAuthRuleManager;
import com.alibaba.cloud.governance.auth.manager.JwtRuleManager;
import com.alibaba.cloud.governance.auth.manager.TargetRuleManager;

public class AuthData {

	private HeaderRuleManager headerRuleManager = new HeaderRuleManager();

	private IdentityRuleManager identityRuleManager = new IdentityRuleManager();

	private IpBlockRuleManager ipBlockRuleManager = new IpBlockRuleManager();

	private JwtAuthRuleManager jwtAuthRuleManager = new JwtAuthRuleManager();

	private JwtRuleManager jwtRuleManager = new JwtRuleManager();

	private TargetRuleManager targetRuleManager = new TargetRuleManager();

	public AuthData() {

	}

	AuthData(HeaderRuleManager headerRuleManager, IdentityRuleManager identityRuleManager,
			IpBlockRuleManager ipBlockRuleManager, JwtAuthRuleManager jwtAuthRuleManager,
			JwtRuleManager jwtRuleManager, TargetRuleManager targetRuleManager) {
		this.headerRuleManager = headerRuleManager;
		this.identityRuleManager = identityRuleManager;
		this.ipBlockRuleManager = ipBlockRuleManager;
		this.jwtAuthRuleManager = jwtAuthRuleManager;
		this.jwtRuleManager = jwtRuleManager;
		this.targetRuleManager = targetRuleManager;
	}

	public HeaderRuleManager getHeaderRuleManager() {
		return headerRuleManager;
	}

	public IdentityRuleManager getIdentityRuleManager() {
		return identityRuleManager;
	}

	public IpBlockRuleManager getIpBlockRuleManager() {
		return ipBlockRuleManager;
	}

	public JwtAuthRuleManager getJwtAuthRuleManager() {
		return jwtAuthRuleManager;
	}

	public JwtRuleManager getJwtRuleManager() {
		return jwtRuleManager;
	}

	public TargetRuleManager getTargetRuleManager() {
		return targetRuleManager;
	}

}
