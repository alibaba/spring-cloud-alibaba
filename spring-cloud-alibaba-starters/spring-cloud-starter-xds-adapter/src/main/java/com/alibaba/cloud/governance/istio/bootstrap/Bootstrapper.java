/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.istio.bootstrap;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.alibaba.cloud.governance.istio.exception.XdsInitializationException;
import io.envoyproxy.envoy.config.core.v3.Node;
import io.grpc.ChannelCredentials;

public abstract class Bootstrapper {

	public abstract BootstrapInfo bootstrap() throws XdsInitializationException;

	BootstrapInfo bootstrap(Map<String, ?> rawData) throws XdsInitializationException {
		throw new UnsupportedOperationException();
	}

	public abstract static class ServerInfo {

		public abstract String target();

		abstract ChannelCredentials channelCredentials();

		abstract boolean useProtocolV3();

		abstract boolean ignoreResourceDeletion();

	}

	public abstract static class CertificateProviderInfo {

		public abstract String pluginName();

		public abstract Map<String, ?> config();

	}

	public abstract static class BootstrapInfo {

		public abstract List<ServerInfo> servers();

		public abstract Map<String, CertificateProviderInfo> certProviders();

		public abstract Node node();

		public abstract String serverListenerResourceNameTemplate();

		abstract static class Builder {

			abstract Builder servers(List<ServerInfo> servers);

			abstract Builder node(Node node);

			abstract Builder certProviders(
					@Nullable Map<String, CertificateProviderInfo> certProviders);

			abstract Builder serverListenerResourceNameTemplate(
					@Nullable String serverListenerResourceNameTemplate);

			abstract BootstrapInfo build();

		}

	}

}
