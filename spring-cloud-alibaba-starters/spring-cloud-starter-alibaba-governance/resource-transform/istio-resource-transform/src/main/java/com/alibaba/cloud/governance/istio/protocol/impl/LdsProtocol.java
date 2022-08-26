package com.alibaba.cloud.governance.istio.protocol.impl;

import com.alibaba.cloud.governance.common.rules.AndRule;
import com.alibaba.cloud.governance.common.rules.OrRule;
import com.alibaba.cloud.governance.common.rules.auth.*;
import com.alibaba.cloud.governance.common.rules.manager.*;
import com.alibaba.cloud.governance.common.rules.util.HeaderMatchUtil;
import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.Permission;
import io.envoyproxy.envoy.config.rbac.v3.Policy;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtAuthentication;
import io.envoyproxy.envoy.extensions.filters.http.jwt_authn.v3.JwtProvider;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

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

	public LdsProtocol(XdsChannel xdsChannel, XdsConfigProperties xdsConfigProperties) {
		super(xdsChannel, xdsConfigProperties);
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
				listeners.add(listener);
			}
			catch (Exception e) {
				// TODO: print with slf4j
				e.printStackTrace();
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

	public void clearLdsCache() {
		IdentityRuleManager.clear();
		IpBlockRuleManager.clear();
		JwtAuthRuleManager.clear();
		JwtRuleManager.clear();
		TargetRuleManager.clear();
		HeaderRuleManager.clear();
	}

	public void resolveAuthRules(List<Listener> listeners) {
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
						resolvePrincipal(entry.getKey(), principal, true);
						break;
					case DENY:
						resolvePrincipal(entry.getKey(), principal, false);
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
						resolvePermission(entry.getKey(), permission, true);
						break;
					case DENY:
						resolvePermission(entry.getKey(), permission, false);
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
				JwtRuleManager.addJwtRule(new JwtRule(entry.getKey(),
						provider.getFromHeadersList(), provider.getIssuer(),
						new ArrayList<>(provider.getAudiencesList()),
						provider.getLocalJwks().getInlineString(),
						new ArrayList<>(provider.getFromParamsList()),
						provider.getForwardPayloadHeader(), provider.getForward()));
			}
		}
		log.info("auth rules resolve finish, RBAC rules {}, Jwt rules {}",
				rbacList.size(), jwtRules.size());
	}

	private void resolvePrincipal(String name, Principal principal, boolean isAllowed) {
		Principal.Set andIds = principal.getAndIds();
		AndRule<CidrRange> ipBlockList = new AndRule<>();
		AndRule<CidrRange> remoteIpBlockList = new AndRule<>();
		AndRule<StringMatcher> requestPrincipalList = new AndRule<>();
		AndRule<StringMatcher> authAudienceList = new AndRule<>();
		AndRule<StringMatcher> authPresenterList = new AndRule<>();
		Map<String, AndRule<ListMatcher>> authClaimMap = new HashMap<>();
		Map<String, AndRule<HeaderMatcher>> headerMap = new HashMap<>();
		AndRule<StringMatcher> identityList = new AndRule<>();
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
			List<CidrRange> ipBlocks = new ArrayList<>();
			List<CidrRange> remoteIpBlocks = new ArrayList<>();
			List<StringMatcher> requestPrincipals = new ArrayList<>();
			List<StringMatcher> authAudiences = new ArrayList<>();
			List<StringMatcher> authPresenters = new ArrayList<>();
			Map<String, List<ListMatcher>> authClaims = new HashMap<>();
			Map<String, List<HeaderMatcher>> headers = new HashMap<>();
			for (Principal orId : orIds.getIdsList()) {
				if (orId.hasAuthenticated()
						&& orId.getAuthenticated().hasPrincipalName()) {
					identities.add(orId.getAuthenticated().getPrincipalName());
				}
				if (orId.hasDirectRemoteIp()) {
					ipBlocks.add(orId.getDirectRemoteIp());
				}
				if (orId.hasRemoteIp()) {
					remoteIpBlocks.add(orId.getRemoteIp());
				}
				if (orId.hasMetadata()
						&& ISTIO_AUTHN.equals(orId.getMetadata().getFilter())) {
					List<MetadataMatcher.PathSegment> segments = orId.getMetadata()
							.getPathList();
					switch (segments.get(0).getKey()) {
					case REQUEST_AUTH_PRINCIPAL:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							requestPrincipals
									.add(orId.getMetadata().getValue().getStringMatch());
						}
						break;
					case REQUEST_AUTH_AUDIENCE:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							authAudiences
									.add(orId.getMetadata().getValue().getStringMatch());
						}
						break;
					case REQUEST_AUTH_PRESENTER:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasStringMatch()) {
							authPresenters
									.add(orId.getMetadata().getValue().getStringMatch());
						}
						break;
					case REQUEST_AUTH_CLAIMS:
						if (orId.hasMetadata() && orId.getMetadata().hasValue()
								&& orId.getMetadata().getValue().hasListMatch()) {
							if (segments.size() >= 2) {
								List<ListMatcher> listMatchers = authClaims.getOrDefault(
										segments.get(1).getKey(), new ArrayList<>());
								listMatchers.add(
										orId.getMetadata().getValue().getListMatch());
								authClaims.put(segments.get(1).getKey(), listMatchers);
							}
						}
						break;
					default:
					}
				}
				if (orId.hasHeader()) {
					List<HeaderMatcher> headerMatchers = headers
							.getOrDefault(orId.getHeader().getName(), new ArrayList<>());
					headerMatchers.add(orId.getHeader());
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
			for (Map.Entry<String, List<ListMatcher>> entry : authClaims.entrySet()) {
				AndRule<ListMatcher> authClaimEntries = authClaimMap
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
			IpBlockRuleManager.addIpBlockRules(
					new IpBlockRule(name, ipBlockList, remoteIpBlockList), isAllowed);
		}
		if (!requestPrincipalList.isEmpty() || !authAudienceList.isEmpty()
				|| !authClaimMap.isEmpty() || !authPresenterList.isEmpty()) {
			JwtAuthRuleManager.addJwtAuthRule(new JwtAuthRule(name, requestPrincipalList,
					authAudienceList, authClaimMap, authPresenterList), isAllowed);
		}
		if (!identityList.isEmpty()) {
			IdentityRuleManager.addIdentityRule(new IdentityRule(name, identityList),
					isAllowed);
		}
		if (!headerMap.isEmpty()) {
			HeaderRuleManager.addHttpHeaderRule(new HttpHeaderRule(name, headerMap),
					isAllowed);
		}
	}

	private void resolvePermission(String name, Permission permission,
			boolean isAllowed) {
		Permission.Set andRules = permission.getAndRules();
		AndRule<StringMatcher> hostList = new AndRule<>();
		AndRule<Integer> portList = new AndRule<>();
		AndRule<StringMatcher> methodList = new AndRule<>();
		AndRule<StringMatcher> pathList = new AndRule<>();
		AndRule<CidrRange> destIpList = new AndRule<>();
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
			List<CidrRange> destIps = new ArrayList<>();
			for (Permission orRule : orRules.getRulesList()) {
				int port = orRule.getDestinationPort();
				if (port > MIN_PORT && port <= MAX_PORT) {
					ports.add(port);
				}
				if (orRule.hasHeader()) {
					switch (orRule.getHeader().getName()) {
					case HEADER_NAME_AUTHORITY:
						hosts.add(HeaderMatchUtil
								.headerMatch2StringMatch(orRule.getHeader()));
						break;
					case HEADER_NAME_METHOD:
						methods.add(HeaderMatchUtil
								.headerMatch2StringMatch(orRule.getHeader()));
						break;
					}
				}
				if (orRule.hasUrlPath() && orRule.getUrlPath().hasPath()) {
					paths.add(orRule.getUrlPath().getPath());
				}
				if (orRule.hasDestinationIp()) {
					destIps.add(orRule.getDestinationIp());
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
			TargetRuleManager.addTargetRules(
					new TargetRule(name, hostList, portList, methodList, pathList),
					isAllowed);
		}
		if (!destIpList.isEmpty()) {
			IpBlockRuleManager.updateDestIpRules(name, destIpList, isAllowed);
		}
	}

}
