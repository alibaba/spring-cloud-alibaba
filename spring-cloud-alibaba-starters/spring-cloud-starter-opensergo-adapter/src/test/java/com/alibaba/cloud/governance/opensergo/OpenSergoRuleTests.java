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

package com.alibaba.cloud.governance.opensergo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.alibaba.cloud.commons.governance.routing.UnifiedRoutingDataStructure;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.config.route.v3.Route;
import io.envoyproxy.envoy.config.route.v3.RouteAction;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.config.route.v3.RouteMatch;
import io.envoyproxy.envoy.config.route.v3.VirtualHost;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author panxiaojun233
 * @author <a href="m13201628570@163.com"></a>
 */
public class OpenSergoRuleTests {

	private static final Logger log = LoggerFactory.getLogger(OpenSergoRuleTests.class);

	private OpenSergoTrafficRouterParser openSergoTrafficRouterParser = new OpenSergoTrafficRouterParser();

	@Test
	public void testOpenSergoTrafficRouterTransform() throws Exception {
		HeaderMatcher headerMatcher = HeaderMatcher.newBuilder().setName("x-tag")
				.setStringMatch(StringMatcher.newBuilder().setExact("v2").buildPartial())
				.build();
		RouteMatch routeMatch = RouteMatch.newBuilder().addHeaders(headerMatcher).build();
		Route route = Route.newBuilder().setMatch(routeMatch)
				.setRoute(RouteAction.newBuilder()
						.setCluster(
								"outbound||v2|service-provider.default.svc.cluster.local")
						.build())
				.build();
		VirtualHost virtualHost = VirtualHost.newBuilder().setName("service-provider")
				.addDomains("service-provider.default.svc.cluster.local").addRoutes(route)
				.build();
		RouteConfiguration routeConfiguration = RouteConfiguration.newBuilder()
				.setName("service-provider").addVirtualHosts(virtualHost).build();
		List<RouteConfiguration> routeConfigurations = new ArrayList<>();
		routeConfigurations.add(routeConfiguration);
		Collection<UnifiedRoutingDataStructure> rules = openSergoTrafficRouterParser
				.resolveLabelRouting(routeConfigurations);
		Assert.assertEquals(rules.size(), 1);
	}

}
