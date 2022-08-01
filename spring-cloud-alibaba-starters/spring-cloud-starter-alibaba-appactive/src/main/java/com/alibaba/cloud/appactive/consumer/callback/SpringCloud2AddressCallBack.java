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

package com.alibaba.cloud.appactive.consumer.callback;

import java.util.List;

import com.alibaba.cloud.appactive.consumer.ServerMeta;
import com.netflix.loadbalancer.Server;
import io.appactive.java.api.base.exception.ExceptionFactory;
import io.appactive.java.api.bridge.rpc.constants.bo.RPCInvokerBO;
import io.appactive.java.api.bridge.rpc.consumer.RPCAddressCallBack;

/**
 * @author raozihao, mageekchiu
 * @author <a href="mailto:zihaorao@gmail.com">Steve</a>
 */
public class SpringCloud2AddressCallBack<T> implements RPCAddressCallBack<T> {

	private final ServerMeta serverMeta;

	public SpringCloud2AddressCallBack(ServerMeta serverMeta) {
		this.serverMeta = serverMeta;
	}

	@Override
	public String getMetaMapValue(T server, String key) {
		if (server == null) {
			return null;
		}
		return getMetaMap(server, key);
	}

	@Override
	public String getServerToString(T server) {
		if (server == null) {
			return null;
		}
		Server thisServer = getServer(server);
		return thisServer.getHost();
	}

	@Override
	public List<RPCInvokerBO<T>> changeToRPCInvokerBOList(List<T> servers) {
		return null;
	}

	@Override
	public List<T> changedToOriginalInvokerList(List<RPCInvokerBO<T>> RPCInvokerBOS) {
		return null;
	}

	private String getMetaMap(T server, String key) {
		Server thisServer = getServer(server);
		if (server == null) {
			return null;
		}
		return serverMeta.getMetaMap(thisServer).get(key);
	}

	private Server getServer(T server) {
		if (server instanceof Server) {
			return (Server) server;
		}
		throw ExceptionFactory
				.makeFault("wrong type for SpringCloud callback:" + server.getClass());
	}

}
