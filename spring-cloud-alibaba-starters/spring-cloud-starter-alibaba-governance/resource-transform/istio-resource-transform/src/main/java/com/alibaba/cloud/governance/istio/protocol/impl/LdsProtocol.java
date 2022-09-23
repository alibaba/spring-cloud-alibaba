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

package com.alibaba.cloud.governance.istio.protocol.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.alibaba.cloud.governance.auth.cache.AuthData;
import com.alibaba.cloud.governance.auth.cache.AuthRepository;
import com.alibaba.cloud.governance.auth.rule.HttpHeaderRule;
import com.alibaba.cloud.governance.auth.rule.IdentityRule;
import com.alibaba.cloud.governance.auth.rule.IpBlockRule;
import com.alibaba.cloud.governance.auth.rule.JwtAuthRule;
import com.alibaba.cloud.governance.auth.rule.JwtRule;
import com.alibaba.cloud.governance.auth.rule.TargetRule;
import com.alibaba.cloud.governance.common.matcher.HeaderMatcher;
import com.alibaba.cloud.governance.common.matcher.IpMatcher;
import com.alibaba.cloud.governance.common.matcher.StringMatcher;
import com.alibaba.cloud.governance.common.rule.AndRule;
import com.alibaba.cloud.governance.common.rule.OrRule;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsScheduledThreadPool;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import com.alibaba.cloud.governance.istio.util.ConvUtil;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.Filter;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.config.rbac.v3.Policy;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtHeader;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.Rds;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdsProtocol extends AbstractXdsProtocol<Listener> {

	private static final Logger log = LoggerFactory.getLogger(LdsProtocol.class);

	private static final String VIRTUAL_INBOUND = "virtualInbound";

	private static final String CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";

	private static final String RBAC_FILTER = "envoy.filters.http.rbac";

	private static final String JWT_FILTER = "envoy.filters.http.jwt_authn";

	private static final String ISTIO_AUTHN = "istio_authn";

	private static final String REQUEST_AUTH_PRINCIPAL = "request.auth.principal";

	private static final String REQUEST_AUTH_AUDIENCE = "request.auth.audiences";

	private static final String REQUEST_AUTH_PRESENTER = "request.auth.presenter";

	private static final String REQUEST_AUTH_CLAIMS = "request.auth.claims";

	private static final String HEADER_NAME_AUTHORITY = ":authority";

	private static final String HEADER_NAME_METHOD = ":method";

	private static final int MIN_PORT = 0;

	private static final int MAX_PORT = 65535;

	private AuthRepository authRepository;

	public LdsProtocol(XdsChannel xdsChannel,
			XdsScheduledThreadPool xdsScheduledThreadPool, int pollingTime,
			AuthRepository authRepository) {
		super(xdsChannel, xdsScheduledThreadPool, pollingTime);
		this.authRepository = authRepository;
	}

	@Override
	public String getTypeUrl() {
		return "type.googleapis.com/envoy.config.listener.v3.Listener";
	}

	@Override
	public List<Listener> decodeXdsResponse(DiscoveryResponse response) {
		List<Listener> listeners = new ArrayList<Listener>();
		for (com.google.protobuf.Any res : response.getResourcesList()) {
			try {
				Listener listener = res.unpack(Listener.class);
				if (listener != null) {
					listeners.add(listener);
				}
			}
			catch (Exception e) {
				log.error("unpack listeners failed", e);
			}
		}
		return listeners;
	}

	private List<RBAC> resolveRbac(List<HttpFilter> httpFilters) {
		return httpFilters.stream()
				.filter(httpFilter -> httpFilter.getName().equals(RBAC_FILTER))
				.map(httpFilter -> {
					try {
						return httpFilter.getTypedConfig().unpack(
								io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC.class);
					}
					catch (InvalidProtocolBufferException e) {
						return null;
					}
				}).filter(Objects::nonNull)
				.map(io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC::getRules)
				.collect(Collectors.toList());
	}

	private List<HttpFilter> resolveHttpFilter(List<Listener> listeners) {
		return listeners.stream()
				.filter(listener -> listener.getName().equals(VIRTUAL_INBOUND))
				.map(Listener::getFilterChainsList)
				.flatMap(filterChains -> filterChains.stream()
						.map(FilterChain::getFiltersList))
				.flatMap(filters -> filters.stream()
						.filter(filter -> filter.getName().equals(CONNECTION_MANAGER)))
				.map(filter -> unpackHttpConnectionManager(filter.getTypedConfig()))
				.filter(Objects::nonNull)
				.flatMap(httpConnectionManager -> httpConnectionManager
						.getHttpFiltersList().stream())
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	private List<JwtAuthentication> resolveJWT(List<HttpFilter> httpFilters) {
		return httpFilters.stream()
				.filter(httpFilter -> httpFilter.getName().equals(JWT_FILTER))
				.map(httpFilter -> {
					try {
						return httpFilter.getTypedConfig()
								.unpack(JwtAuthentication.class);
					}
					catch (InvalidProtocolBufferException e) {
						return null;
					}
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private HttpConnectionManager unpackHttpConnectionManager(Any any) {
		try {
			if (!any.is(HttpConnectionManager.class)) {
				return null;
			}
			return any.unpack(HttpConnectionManager.class);
		}
		catch (InvalidProtocolBufferException e) {
			return null;
		}
	}

	public Set<String> getRouteNames(List<Listener> listeners) {
		Set<String> routeNames = new HashSet<>();
		listeners.forEach(listener -> routeNames.addAll(listener.getFilterChainsList()
				.stream().flatMap((e) -> e.getFiltersList().stream())
				.map(Filter::getTypedConfig).map(this::unpackHttpConnectionManager)
				.filter(Objects::nonNull).map(HttpConnectionManager::getRds)
				.map(Rds::getRouteConfigName).filter(StringUtils::isNotEmpty)
				.collect(Collectors.toList())));
		return routeNames;
	}

	public void resolveAuthRules(List<Listener> listeners) {
		if (listeners == null || listeners.isEmpty()) {
			return;
		}
		AuthData authData = new AuthData();
		List<HttpFilter> httpFilters = resolveHttpFilter(listeners);
		List<RBAC> rbacList = resolveRbac(httpFilters);
		for (RBAC rbac : rbacList) {
			for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
				// principals
				List<Principal> principals = entry.getValue().getPrincipalsList();
				for (Principal principal : principals) {
					switch (rbac.getAction()) {
					case ALLOW:
					case UNRECOGNIZED:
						resolvePrincipal(authData, entry.getKey(), principal, true);
						break;
					case DENY:
						resolvePrincipal(authData, entry.getKey(), principal, false);
						break;
					default:
						break;
					}
				}
				// permission
				List<Permission> permissions = entry.getValue().getPermissionsList();
				for (Permission permission : permissions) {
					switch (rbac.getAction()) {
					case ALLOW:
					case UNRECOGNIZED:
						resolvePermission(authData, entry.getKey(), permission, true);
						break;
					case DENY:
						resolvePermission(authData, entry.getKey(), permission, false);
						break;
					default:
						break;
					}
				}
			}
		}
		List<JwtAuthentication> jwtRules = resolveJWT(httpFilters);
		for (JwtAuthentication jwtRule : jwtRules) {
			Map<String, JwtProvider> jwtProviders = jwtRule.getProvidersMap();
			for (Map.Entry<String, JwtProvider> entry : jwtProviders.entrySet()) {
				JwtProvider provider = entry.getValue();
				Map<String, String> fromHeaders = new HashMap<>();
				for (JwtHeader header : provider.getFromHeadersList()) {
					fromHeaders.put(header.getName(), header.getValuePrefix());
				}
				authData.getJwtRuleManager().getJwtRules().put(entry.getKey(),
						new JwtRule(entry.getKey(), fromHeaders, provider.getIssuer(),
								new ArrayList<>(provider.getAudiencesList()),
								provider.getLocalJwks().getInlineString(),
								new ArrayList<>(provider.getFromParamsList()),
								provider.getForwardPayloadHeader(),
								provider.getForward()));
			}
		}
		log.info("auth rules resolve finish, RBAC rules {}, Jwt rules {}",
				rbacList.size(), jwtRules.size());
		authRepository.setAuthData(authData);
	}

	private void resolvePrincipal(AuthData authData, String name, Principal principal,
			boolean isAllowed) {
		Principal.Set andIds = principal.getAndIds();
		AndRule<IpMatcher> ipBlockList = new AndRule<>();
		AndRule<IpMatcher> remoteIpBlockList = new AndRule<>();
		AndRule<StringMatcher> requestPrincipalList = new AndRule<>();
		AndRule<StringMatcher> authAudienceList = new AndRule<>();
		AndRule<StringMatcher> authPresenterList = new AndRule<>();
		Map<String, AndRule<StringMatcher>> authClaimMap = new HashMap<>();
		Map<String, AndRule<HeaderMatcher>> headerMap = new HashMap<>();
		AndRule<StringMatcher> identityList = new AndRule<>();

		Map<String, HttpHeaderRule> allowHeaderRules = authData.getHeaderRuleManager()
				.getAllowHeaderRules();
		Map<String, HttpHeaderRule> denyHeaderRules = authData.getHeaderRuleManager()
				.getDenyHeaderRules();
		Map<String, IdentityRule> allowIdentityRules = authData.getIdentityRuleManager()
				.getAllowIdentityRules();
		Map<String, IdentityRule> denyIdentityRules = authData.getIdentityRuleManager()
				.getDenyIdentityRules();
		Map<String, IpBlockRule> allowIpBlockRules = authData.getIpBlockRuleManager()
				.getAllowIpBlockRules();
		Map<String, IpBlockRule> denyIpBlockRules = authData.getIpBlockRuleManager()
				.getDenyIpBlockRules();
		Map<String, JwtAuthRule> allowJwtAuthRules = authData.getJwtAuthRuleManager()
				.getAllowJwtAuthRules();
		Map<String, JwtAuthRule> denyJwtAuthRules = authData.getJwtAuthRuleManager()
				.getDenyJwtAuthRules();

		for (Principal andId : andIds.getIdsList()) {
			if (andId.getAny()) {
				return;
			}
			boolean isNot = false;
			if (andId.hasNotId()) {
				isNot = true;
				andId = andId.getNotId();
			}
			Principal.Set orIds = andId.getOrIds();
			List<StringMatcher> identities = new ArrayList<>();
			List<IpMatcher> ipBlocks = new ArrayList<>();
			List<IpMatcher> remoteIpBlocks = new ArrayList<>();
			List<StringMatcher> requestPrincipals = new ArrayList<>();
			List<StringMatcher> authAudiences = new ArrayList<>();
			List<StringMatcher> authPresenters = new ArrayList<>();
			Map<String, List<StringMatcher>> authClaims = new HashMap<>();
			Map<String, List<HeaderMatcher>> headers = new HashMap<>();
			for (Principal orId : orIds.getIdsList()) {
				if (orId.hasAuthenticated()
						&& orId.getAuthenticated().hasPrincipalName()) {
					StringMatcher identity = ConvUtil.convStringMatcher(
							orId.getAuthenticated().getPrincipalName());
					if (identity != null) {
						identities.add(identity);
					}
				}
				if (orId.hasDirectRemoteIp()) {
					ipBlocks.add(ConvUtil.convertIpMatcher(orId.getDirectRemoteIp()));
				}
				if (orId.hasRemoteIp()) {
					remoteIpBlocks.add(ConvUtil.convertIpMatcher(orId.getRemoteIp()));
				}
				if (orId.hasMetadata()
						&& ISTIO_AUTHN.equals(orId.getMetadata().getFilter())) {
					List<MetadataMatcher.PathSegment> segments = orId.getMetadata()
							.getPathList();
					switch (segments.get(0).getKey()) {
					case REQUEST_AUTH_PRINCIPAL:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							StringMatcher requestPrinciple = ConvUtil.convStringMatcher(
									orId.getMetadata().getValue().getStringMatch());
							if (requestPrinciple != null) {
								requestPrincipals.add(requestPrinciple);
							}
						}
						break;
					case REQUEST_AUTH_AUDIENCE:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							StringMatcher authAudience = ConvUtil.convStringMatcher(
									orId.getMetadata().getValue().getStringMatch());
							if (authAudience != null) {
								authAudiences.add(authAudience);
							}
						}
						break;
					case REQUEST_AUTH_PRESENTER:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							StringMatcher authPresenter = ConvUtil.convStringMatcher(
									orId.getMetadata().getValue().getStringMatch());
							if (authPresenter != null) {
								authPresenters.add(authPresenter);
							}
						}
						break;
					case REQUEST_AUTH_CLAIMS:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasListMatch()) {
							if (segments.size() >= 2) {
								List<StringMatcher> stringMatchers = authClaims
										.getOrDefault(segments.get(1).getKey(),
												new ArrayList<>());
								StringMatcher stringMatcher = null;
								try {
									stringMatcher = ConvUtil.convStringMatcher(
											orId.getMetadata().getValue().getListMatch()
													.getOneOf().getStringMatch());
								}
								catch (Exception e) {
									log.error(
											"unable to get/convert request auth claims");
								}
								if (stringMatcher != null) {
									stringMatchers.add(stringMatcher);
								}
								if (!stringMatchers.isEmpty()) {
									authClaims.put(segments.get(1).getKey(),
											stringMatchers);
								}
							}
						}
						break;
					default:
					}
				}
				if (orId.hasHeader()) {
					List<HeaderMatcher> headerMatchers = headers
							.getOrDefault(orId.getHeader().getName(), new ArrayList<>());
					HeaderMatcher headerMatcher = ConvUtil
							.convertHeaderMatcher(orId.getHeader());
					headerMatchers.add(headerMatcher);
					headers.put(orId.getHeader().getName(), headerMatchers);
				}
			}
			if (!identities.isEmpty()) {
				identityList.addOrRule(new OrRule<>(identities, isNot));
			}
			if (!requestPrincipals.isEmpty()) {
				requestPrincipalList.addOrRule(new OrRule<>(requestPrincipals, isNot));
			}
			if (!authAudiences.isEmpty()) {
				authAudienceList.addOrRule(new OrRule<>(authAudiences, isNot));
			}
			if (!authPresenters.isEmpty()) {
				authPresenterList.addOrRule(new OrRule<>(authPresenters, isNot));
			}
			if (!ipBlocks.isEmpty()) {
				ipBlockList.addOrRule(new OrRule<>(ipBlocks, isNot));
			}
			if (!remoteIpBlocks.isEmpty()) {
				remoteIpBlockList.addOrRule(new OrRule<>(remoteIpBlocks, isNot));
			}
			for (Map.Entry<String, List<StringMatcher>> entry : authClaims.entrySet()) {
				AndRule<StringMatcher> authClaimEntries = authClaimMap
						.getOrDefault(entry.getKey(), new AndRule<>());
				authClaimEntries.addOrRule(new OrRule<>(entry.getValue(), isNot));
				authClaimMap.put(entry.getKey(), authClaimEntries);
			}
			for (Map.Entry<String, List<HeaderMatcher>> entry : headers.entrySet()) {
				AndRule<HeaderMatcher> headerEntries = headerMap
						.getOrDefault(entry.getKey(), new AndRule<>());
				headerEntries.addOrRule(new OrRule<>(entry.getValue(), isNot));
				headerMap.put(entry.getKey(), headerEntries);
			}
		}
		if (!ipBlockList.isEmpty() || !remoteIpBlockList.isEmpty()) {
			IpBlockRule ipBlockRule = new IpBlockRule(name, ipBlockList,
					remoteIpBlockList);
			if (isAllowed) {
				allowIpBlockRules.put(name, ipBlockRule);
			}
			else {
				denyIpBlockRules.put(name, ipBlockRule);
			}
		}
		if (!requestPrincipalList.isEmpty() || !authAudienceList.isEmpty()
				|| !authClaimMap.isEmpty() || !authPresenterList.isEmpty()) {
			JwtAuthRule jwtAuthRule = new JwtAuthRule(name, requestPrincipalList,
					authAudienceList, authClaimMap, authPresenterList);
			if (isAllowed) {
				allowJwtAuthRules.put(name, jwtAuthRule);
			}
			else {
				denyJwtAuthRules.put(name, jwtAuthRule);
			}
		}
		if (!identityList.isEmpty()) {
			IdentityRule identityRule = new IdentityRule(name, identityList);
			if (isAllowed) {
				allowIdentityRules.put(name, identityRule);
			}
			else {
				denyIdentityRules.put(name, identityRule);
			}
		}
		if (!headerMap.isEmpty()) {
			HttpHeaderRule headerRule = new HttpHeaderRule(name, headerMap);
			if (isAllowed) {
				allowHeaderRules.put(name, headerRule);
			}
			else {
				denyHeaderRules.put(name, headerRule);
			}
		}
	}

	private void resolvePermission(AuthData authData, String name, Permission permission,
			boolean isAllowed) {
		Permission.Set andRules = permission.getAndRules();
		AndRule<StringMatcher> hostList = new AndRule<>();
		AndRule<Integer> portList = new AndRule<>();
		AndRule<StringMatcher> methodList = new AndRule<>();
		AndRule<StringMatcher> pathList = new AndRule<>();
		AndRule<IpMatcher> destIpList = new AndRule<>();
		Map<String, IpBlockRule> allowIpBlockRules = authData.getIpBlockRuleManager()
				.getAllowIpBlockRules();
		Map<String, IpBlockRule> denyIpBlockRules = authData.getIpBlockRuleManager()
				.getDenyIpBlockRules();
		Map<String, TargetRule> allowTargetRules = authData.getTargetRuleManager()
				.getAllowTargetRules();
		Map<String, TargetRule> denyTargetRules = authData.getTargetRuleManager()
				.getDenyTargetRules();
		for (Permission andRule : andRules.getRulesList()) {
			if (andRule.getAny()) {
				return;
			}
			boolean isNot = false;
			if (andRule.hasNotRule()) {
				isNot = true;
				andRule = andRule.getNotRule();
			}
			Permission.Set orRules = andRule.getOrRules();
			List<StringMatcher> hosts = new ArrayList<>();
			List<Integer> ports = new ArrayList<>();
			List<StringMatcher> methods = new ArrayList<>();
			List<StringMatcher> paths = new ArrayList<>();
			List<IpMatcher> destIps = new ArrayList<>();
			for (Permission orRule : orRules.getRulesList()) {
				int port = orRule.getDestinationPort();
				if (port > MIN_PORT && port <= MAX_PORT) {
					ports.add(port);
				}
				if (orRule.hasHeader()) {
					switch (orRule.getHeader().getName()) {
					case HEADER_NAME_AUTHORITY:
						StringMatcher host = ConvUtil
								.convStringMatcher(orRule.getHeader());
						if (host != null) {
							hosts.add(host);
						}
						break;
					case HEADER_NAME_METHOD:
						StringMatcher method = ConvUtil
								.convStringMatcher(orRule.getHeader());
						if (method != null) {
							methods.add(method);
						}
						break;
					}
				}
				if (orRule.hasUrlPath() && orRule.getUrlPath().hasPath()) {
					StringMatcher path = ConvUtil
							.convStringMatcher(orRule.getUrlPath().getPath());
					if (path != null) {
						paths.add(path);
					}
				}
				if (orRule.hasDestinationIp()) {
					destIps.add(ConvUtil.convertIpMatcher(orRule.getDestinationIp()));
				}
			}
			if (!hosts.isEmpty()) {
				hostList.addOrRule(new OrRule<>(hosts, isNot));
			}
			if (!ports.isEmpty()) {
				portList.addOrRule(new OrRule<>(ports, isNot));
			}
			if (!methods.isEmpty()) {
				methodList.addOrRule(new OrRule<>(methods, isNot));
			}
			if (!paths.isEmpty()) {
				pathList.addOrRule(new OrRule<>(paths, isNot));
			}
			if (!destIps.isEmpty()) {
				destIpList.addOrRule(new OrRule<>(destIps, isNot));
			}
		}
		if (!hostList.isEmpty() || !portList.isEmpty() || !methodList.isEmpty()
				|| !pathList.isEmpty()) {
			TargetRule targetRule = new TargetRule(name, hostList, portList, methodList,
					pathList);
			if (isAllowed) {
				allowTargetRules.put(name, targetRule);
			}
			else {
				denyTargetRules.put(name, targetRule);
			}
		}
		if (!destIpList.isEmpty()) {
			if (isAllowed) {
				allowIpBlockRules.get(name).setDestIps(destIpList);
			}
			else {
				denyIpBlockRules.get(name).setDestIps(destIpList);
			}
		}
	}

}
