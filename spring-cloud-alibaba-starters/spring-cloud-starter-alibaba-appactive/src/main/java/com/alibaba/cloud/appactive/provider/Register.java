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

package com.alibaba.cloud.appactive.provider;

import java.util.Map;

import com.alibaba.cloud.appactive.common.ServiceMetaObject;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import io.appactive.java.api.bridge.rpc.constants.constant.RPCConstant;
import io.appactive.java.api.rule.machine.AbstractMachineUnitRuleService;
import io.appactive.rule.ClientRuleService;

/**
 * @author mageekchiu
 */
public final class Register {

	private Register() {
	}

	private static final AbstractMachineUnitRuleService machineUnitRuleService = ClientRuleService
			.getMachineUnitRuleService();

	public static void doRegisterNacos(NacosRegistration nacosRegistration) {
		Map<String, String> map = nacosRegistration.getMetadata();
		map.put(RPCConstant.URL_UNIT_LABEL_KEY, machineUnitRuleService.getCurrentUnit());
		ServiceMetaObject serviceMetaObject = URIRegister.getServiceMetaObject();
		if (serviceMetaObject == null) {
			return;
		}
		map.put(RPCConstant.SPRING_CLOUD_SERVICE_META, serviceMetaObject.getMeta());
		map.put(RPCConstant.SPRING_CLOUD_SERVICE_META_VERSION,
				serviceMetaObject.getMd5OfList());
	}

}
