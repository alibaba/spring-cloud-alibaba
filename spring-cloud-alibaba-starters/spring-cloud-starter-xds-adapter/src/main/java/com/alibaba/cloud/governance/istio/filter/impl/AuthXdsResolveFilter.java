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

package com.alibaba.cloud.governance.istio.filter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.cloud.commons.governance.auth.condition.AuthCondition;
import com.alibaba.cloud.commons.governance.auth.rule.AuthRule;
import com.alibaba.cloud.commons.governance.auth.rule.AuthRules;
import com.alibaba.cloud.commons.governance.auth.rule.JwtRule;
import com.alibaba.cloud.commons.governance.event.AuthDataChangedEvent;
import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.commons.matcher.PortMatcher;
import com.alibaba.cloud.commons.matcher.StringMatcher;
import com.alibaba.cloud.governance.istio.constant.IstioConstants;
import com.alibaba.cloud.governance.istio.filter.AbstractXdsResolveFilter;
import com.alibaba.cloud.governance.istio.util.ConvUtil;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
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
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthXdsResolveFilter extends AbstractXdsResolveFilter<List<Listener>> {

	@Override
	public boolean resolve(List<Listener> listeners) {
		if (listeners == null || listeners.isEmpty()) {
			return false;
		}
		Map<String, AuthRule> allowAuthRules = new HashMap<>();
		Map<String, AuthRule> denyAuthRules = new HashMap<>();
		Map<String, JwtRule> jwtRules = new HashMap<>();
		List<HttpFilter> httpFilters = resolveHttpFilter(listeners);
		List<RBAC> rbacList = resolveRbac(httpFilters);
		for (RBAC rbac : rbacList) {
			for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
				AuthRule authRule = new AuthRule(AuthRule.RuleOperation.AND);
				AuthRule principalOr = new AuthRule(AuthRule.RuleOperation.OR);
				// principals
				List<Principal> principals = entry.getValue().getPrincipalsList();
				for (Principal principal : principals) {
					AuthRule principalAnd = resolvePrincipal(principal);
					if (principalAnd != null && !principalAnd.isEmpty()) {
						principalOr.addChildren(principalAnd);
					}
				}
				// permission
				AuthRule permissionOr = new AuthRule(AuthRule.RuleOperation.OR);
				List<Permission> permissions = entry.getValue().getPermissionsList();
				for (Permission permission : permissions) {
					AuthRule permissionAnd = resolvePermission(permission);
					if (permissionAnd != null && !permissionAnd.isEmpty()) {
						permissionOr.addChildren(permissionAnd);
					}
				}
				if (!principalOr.isEmpty()) {
					authRule.addChildren(principalOr);
				}
				if (!permissionOr.isEmpty()) {
					authRule.addChildren(permissionOr);
				}
				if (authRule.isEmpty()) {
					continue;
				}
				switch (rbac.getAction()) {
				case UNRECOGNIZED:
				case ALLOW:
					allowAuthRules.put(entry.getKey(), authRule);
					break;
				case DENY:
					denyAuthRules.put(entry.getKey(), authRule);
					break;
				default:
					log.warn("Unknown rbac action, {}", rbac.getAction());
				}
			}
		}
		List<JwtAuthentication> jwtAuthentications = resolveJWT(httpFilters);
		for (JwtAuthentication jwtRule : jwtAuthentications) {
			Map<String, JwtProvider> jwtProviders = jwtRule.getProvidersMap();
			for (Map.Entry<String, JwtProvider> entry : jwtProviders.entrySet()) {
				JwtProvider provider = entry.getValue();
				Map<String, String> fromHeaders = new HashMap<>();
				for (JwtHeader header : provider.getFromHeadersList()) {
					fromHeaders.put(header.getName(), header.getValuePrefix());
				}
				jwtRules.put(entry.getKey(),
						new JwtRule(entry.getKey(), fromHeaders, provider.getIssuer(),
								new ArrayList<>(provider.getAudiencesList()),
								provider.getLocalJwks().getInlineString(),
								new ArrayList<>(provider.getFromParamsList()),
								provider.getForwardPayloadHeader(),
								provider.getForward()));
			}
		}
		log.info("Auth rules resolve finish, RBAC rules size: {}, Jwt rules size: {}",
				allowAuthRules.size() + denyAuthRules.size(), jwtRules.size());
		applicationContext.publishEvent(new AuthDataChangedEvent(this,
				new AuthRules(allowAuthRules, denyAuthRules, jwtRules)));
		return true;
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

	private AuthRule resolvePrincipal(Principal principal) {
		Principal.Set andIds = principal.getAndIds();
		AuthRule andChildren = new AuthRule(AuthRule.RuleOperation.AND);
		for (Principal andId : andIds.getIdsList()) {
			if (andId.getAny()) {
				return null;
			}
			boolean isNot = false;
			if (andId.hasNotId()) {
				isNot = true;
				andId = andId.getNotId();
			}
			AuthRule orChildren = new AuthRule(AuthRule.RuleOperation.OR, isNot);
			Principal.Set orIds = andId.getOrIds();
			for (Principal orId : orIds.getIdsList()) {
				if (orId.hasAuthenticated()
						&& orId.getAuthenticated().hasPrincipalName()) {
					StringMatcher identity = ConvUtil.convStringMatcher(
							orId.getAuthenticated().getPrincipalName());
					if (identity != null) {
						orChildren.addChildren(new AuthRule(new AuthCondition(
								AuthCondition.ValidationType.IDENTITY, identity)));
					}
				}
				if (orId.hasDirectRemoteIp()) {
					orChildren.addChildren(new AuthRule(new AuthCondition(
							AuthCondition.ValidationType.SOURCE_IP,
							ConvUtil.convertIpMatcher(orId.getDirectRemoteIp()))));
				}
				if (orId.hasRemoteIp()) {
					orChildren.addChildren(new AuthRule(
							new AuthCondition(AuthCondition.ValidationType.REMOTE_IP,
									ConvUtil.convertIpMatcher(orId.getRemoteIp()))));
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
								orChildren.addChildren(new AuthRule(new AuthCondition(
										AuthCondition.ValidationType.REQUEST_PRINCIPALS,
										requestPrinciple)));
							}
						}
						break;
					case REQUEST_AUTH_AUDIENCE:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							StringMatcher authAudience = ConvUtil.convStringMatcher(
									orId.getMetadata().getValue().getStringMatch());
							if (authAudience != null) {
								orChildren.addChildren(new AuthRule(new AuthCondition(
										AuthCondition.ValidationType.AUTH_AUDIENCES,
										authAudience)));
							}
						}
						break;
					case REQUEST_AUTH_PRESENTER:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							StringMatcher authPresenter = ConvUtil.convStringMatcher(
									orId.getMetadata().getValue().getStringMatch());
							if (authPresenter != null) {
								orChildren.addChildren(new AuthRule(new AuthCondition(
										AuthCondition.ValidationType.AUTH_PRESENTERS,
										authPresenter)));
							}
						}
						break;
					case REQUEST_AUTH_CLAIMS:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasListMatch()) {
							if (segments.size() >= 2) {
								String key = segments.get(1).getKey();
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
								orChildren.addChildren(new AuthRule(new AuthCondition(
										AuthCondition.ValidationType.AUTH_CLAIMS, key,
										stringMatcher)));
							}
						}
						break;
					default:
					}
				}
				if (orId.hasHeader()) {
					String headerName = orId.getHeader().getName();
					if (StringUtils.isEmpty(headerName)) {
						continue;
					}
					StringMatcher stringMatcher = ConvUtil
							.convertHeaderMatcher(orId.getHeader());
					orChildren.addChildren(new AuthRule(
							new AuthCondition(AuthCondition.ValidationType.HEADER,
									headerName, stringMatcher)));
				}
			}
			if (!orChildren.isEmpty()) {
				andChildren.addChildren(orChildren);
			}
		}
		return andChildren;
	}

	private AuthRule resolvePermission(Permission permission) {
		Permission.Set andRules = permission.getAndRules();
		AuthRule andChildren = new AuthRule(AuthRule.RuleOperation.AND);
		for (Permission andRule : andRules.getRulesList()) {
			if (andRule.getAny()) {
				return null;
			}
			boolean isNot = false;
			if (andRule.hasNotRule()) {
				isNot = true;
				andRule = andRule.getNotRule();
			}
			Permission.Set orRules = andRule.getOrRules();
			AuthRule orChildren = new AuthRule(AuthRule.RuleOperation.OR, isNot);
			for (Permission orRule : orRules.getRulesList()) {
				int port = orRule.getDestinationPort();
				if (port > MIN_PORT && port <= MAX_PORT) {
					orChildren.addChildren(new AuthRule(new AuthCondition(
							AuthCondition.ValidationType.PORTS, new PortMatcher(port))));
				}
				if (orRule.hasHeader()) {
					switch (orRule.getHeader().getName()) {
					case HEADER_NAME_AUTHORITY:
						StringMatcher host = ConvUtil
								.convStringMatcher(orRule.getHeader());
						if (host != null) {
							orChildren.addChildren(new AuthRule(new AuthCondition(
									AuthCondition.ValidationType.HOSTS, host)));
						}
						break;
					case HEADER_NAME_METHOD:
						StringMatcher method = ConvUtil
								.convStringMatcher(orRule.getHeader());
						if (method != null) {
							orChildren.addChildren(new AuthRule(new AuthCondition(
									AuthCondition.ValidationType.METHODS, method)));
						}
						break;
					}
				}
				if (orRule.hasUrlPath() && orRule.getUrlPath().hasPath()) {
					StringMatcher path = ConvUtil
							.convStringMatcher(orRule.getUrlPath().getPath());
					if (path != null) {
						orChildren.addChildren(new AuthRule(new AuthCondition(
								AuthCondition.ValidationType.PATHS, path)));
					}
				}
				if (orRule.hasDestinationIp()) {
					orChildren.addChildren(new AuthRule(new AuthCondition(
							AuthCondition.ValidationType.DEST_IP,
							ConvUtil.convertIpMatcher(orRule.getDestinationIp()))));
				}
			}
			if (!orChildren.isEmpty()) {
				andChildren.addChildren(orChildren);
			}
		}
		return andChildren;
	}

	@Override
	public String getTypeUrl() {
		return IstioConstants.LDS_URL;
	}

}
