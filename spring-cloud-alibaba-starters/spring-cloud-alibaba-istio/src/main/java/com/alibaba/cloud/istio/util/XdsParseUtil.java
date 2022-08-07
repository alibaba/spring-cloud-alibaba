package com.alibaba.cloud.istio.util;

import com.alibaba.cloud.istio.rules.auth.IdentityRule;
import com.alibaba.cloud.istio.rules.auth.IpBlockRule;
import com.alibaba.cloud.istio.rules.auth.JwtAuthRule;
import com.alibaba.cloud.istio.rules.auth.TargetRule;
import com.alibaba.cloud.istio.rules.manager.*;
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
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpFilter;
import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.*;
import java.util.stream.Collectors;

public class XdsParseUtil {
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

    private static List<RBAC> resolveRbac(List<HttpFilter> httpFilters) {
        return httpFilters.stream()
                .filter(httpFilter -> httpFilter.getName().equals(RBAC_FILTER))
                .map(httpFilter -> {
                    try {
                        return httpFilter.getTypedConfig().unpack(io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC.class);
                    } catch (InvalidProtocolBufferException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(io.envoyproxy.envoy.extensions.filters.http.rbac.v3.RBAC::getRules)
                .collect(Collectors.toList());
    }

    private static List<HttpFilter> resolveHttpFilter(List<Listener> listeners) {
        return listeners.stream()
                .filter(listener -> listener.getName().equals(VIRTUAL_INBOUND))
                .map(Listener::getFilterChainsList)
                .flatMap(filterChains -> filterChains.stream().map(FilterChain::getFiltersList))
                .flatMap(filters -> filters.stream().filter(filter -> filter.getName().equals(CONNECTION_MANAGER)))
                .map(filter -> unpackHttpConnectionManager(filter.getTypedConfig()))
                .filter(Objects::nonNull)
                .flatMap(httpConnectionManager -> httpConnectionManager.getHttpFiltersList().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static List<JwtAuthentication> resolveJWT(List<HttpFilter> httpFilters) {
        return httpFilters.stream()
                .filter(httpFilter -> httpFilter.getName().equals(JWT_FILTER))
                .map(httpFilter -> {
                    try {
                        return httpFilter.getTypedConfig().unpack(JwtAuthentication.class);
                    } catch (InvalidProtocolBufferException e) {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private static HttpConnectionManager unpackHttpConnectionManager(Any any) {
        try {
            if (!any.is(HttpConnectionManager.class)) {
                return null;
            }
            return any.unpack(HttpConnectionManager.class);
        } catch (InvalidProtocolBufferException e) {
            return null;
        }
    }

    public static void clearLdsCache() {
        IdentityRuleManager.clear();
        IpBlockRuleManager.clear();
        JwtAuthRuleManager.clear();
        JwtRuleManager.clear();
        TargetRuleManager.clear();
    }

    public static void resolveAuthRules(List<Listener> listeners) {
        List<HttpFilter> httpFilters = resolveHttpFilter(listeners);
        List<RBAC> rbacList = resolveRbac(httpFilters);
        for (RBAC rbac : rbacList) {
            for (Map.Entry<String, Policy> entry : rbac.getPoliciesMap().entrySet()) {
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
            }
        }
        resolveJWT(httpFilters);
    }

    private static void resolvePrincipal(String name, Principal principal, boolean isAllowed) {
        if (!isAllowed) {
            principal = principal.getNotId();
        }
        Principal.Set andIds = principal.getAndIds();
        List<List<CidrRange>> ipBlockList = new ArrayList<>();
        List<List<CidrRange>> remoteIpBlockList = new ArrayList<>();
        List<List<StringMatcher>> requestPrincipalList = new ArrayList<>();
        List<List<StringMatcher>> authAudienceList = new ArrayList<>();
        List<List<StringMatcher>> authPresenterList = new ArrayList<>();
        Map<String, List<List<ListMatcher>>> authClaimMap = new HashMap<>();
        Map<String, List<List<HeaderMatcher>>> headerMap = new HashMap<>();
        List<List<StringMatcher>> identityList = new ArrayList<>();
        for (Principal andId : andIds.getIdsList()) {
            if (andId.getAny()) {
                return;
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
                if (orId.hasAuthenticated() && orId.getAuthenticated().hasPrincipalName()) {
                    identities.add(orId.getAuthenticated().getPrincipalName());
                }
                if (orId.hasDirectRemoteIp()) {
                    ipBlocks.add(orId.getDirectRemoteIp());
                }
                if (orId.hasRemoteIp()) {
                    remoteIpBlocks.add(orId.getRemoteIp());
                }
                if (orId.hasMetadata() && ISTIO_AUTHN.equals(orId.getMetadata().getFilter())) {
                    List<MetadataMatcher.PathSegment> segments = orId.getMetadata().getPathList();
                    switch (segments.get(0).getKey()) {
                        case REQUEST_AUTH_PRINCIPAL:
                            if (orId.hasMetadata() && orId.getMetadata().hasValue() && orId.getMetadata().getValue().hasStringMatch()) {
                                requestPrincipals.add(orId.getMetadata().getValue().getStringMatch());
                            }
                            break;
                        case REQUEST_AUTH_AUDIENCE:
                            if (orId.hasMetadata() && orId.getMetadata().hasValue() && orId.getMetadata().getValue().hasStringMatch()) {
                                authAudiences.add(orId.getMetadata().getValue().getStringMatch());
                            }
                            break;
                        case REQUEST_AUTH_PRESENTER:
                            if (orId.hasMetadata() && orId.getMetadata().hasValue() && orId.getMetadata().getValue().hasStringMatch()) {
                                authPresenters.add(orId.getMetadata().getValue().getStringMatch());
                            }
                            break;
                        case REQUEST_AUTH_CLAIMS:
                            if (orId.hasMetadata() && orId.getMetadata().hasValue() && orId.getMetadata().getValue().hasListMatch()) {
                                if (segments.size() >= 2) {
                                    List<ListMatcher> listMatchers = authClaims.getOrDefault(segments.get(1).getKey(), new ArrayList<>());
                                    listMatchers.add(orId.getMetadata().getValue().getListMatch());
                                    authClaims.put(segments.get(1).getKey(), listMatchers);
                                }
                            }
                            break;
                        default:
                    }
                }
                if (orId.hasHeader()) {
                    List<HeaderMatcher> headerMatchers = headers.getOrDefault(orId.getHeader().getName(), new ArrayList<>());
                    headerMatchers.add(orId.getHeader());
                    headers.put(orId.getHeader().getName(), headerMatchers);
                }
            }
            if (!identities.isEmpty()) {
                identityList.add(identities);
            }
            if (!requestPrincipals.isEmpty()) {
                requestPrincipalList.add(requestPrincipals);
            }
            if (!authAudiences.isEmpty()) {
                authAudienceList.add(authAudiences);
            }
            if (!authPresenters.isEmpty()) {
                authPresenterList.add(authPresenters);
            }
            if (!ipBlocks.isEmpty()) {
                ipBlockList.add(ipBlocks);
            }
            if (!remoteIpBlocks.isEmpty()) {
                remoteIpBlockList.add(remoteIpBlocks);
            }
            for (Map.Entry<String, List<ListMatcher>> entry : authClaims.entrySet()) {
                List<List<ListMatcher>> authClaimEntries = authClaimMap.getOrDefault(entry.getKey(), new ArrayList<>());
                authClaimEntries.add(entry.getValue());
                authClaimMap.put(entry.getKey(), authClaimEntries);
            }
            for (Map.Entry<String, List<HeaderMatcher>> entry : headers.entrySet()) {
                List<List<HeaderMatcher>> headerEntries = headerMap.getOrDefault(entry.getKey(), new ArrayList<>());
                headerEntries.add(entry.getValue());
                headerMap.put(entry.getKey(), headerEntries);
            }
        }
        IpBlockRuleManager.addIpBlockRules(new IpBlockRule(name, ipBlockList, remoteIpBlockList), isAllowed);
        JwtAuthRuleManager.addJwtAuthRule(new JwtAuthRule(name, requestPrincipalList, authAudienceList, authClaimMap, authPresenterList), isAllowed);
        IdentityRuleManager.addIdentityRule(new IdentityRule(name, identityList), isAllowed);
    }

    private static void resolvePermission(String name, Permission permission, boolean isAllowed) {
        if (!isAllowed) {
            permission = permission.getNotRule();
        }
        Permission.Set andRules = permission.getAndRules();
        List<List<StringMatcher>> hostList = new ArrayList<>();
        List<List<Integer>> portList = new ArrayList<>();
        List<List<StringMatcher>> methodList = new ArrayList<>();
        List<List<StringMatcher>> pathList = new ArrayList<>();
        List<List<CidrRange>> destIpList = new ArrayList<>();
        for (Permission andRule : andRules.getRulesList()) {
            if (andRule.getAny()) {
                return;
            }
            Permission.Set orRules = andRule.getOrRules();
            List<StringMatcher> hosts = new ArrayList<>();
            List<Integer> ports = new ArrayList<>();
            List<StringMatcher> methods = new ArrayList<>();
            List<StringMatcher> paths = new ArrayList<>();
            List<CidrRange> destIps = new ArrayList<>();
            for (Permission orRule : orRules.getRulesList()) {
                int port = orRule.getDestinationPort();
                if (port > 0 && port <= 65535) {
                    ports.add(port);
                }
                if (orRule.hasHeader()) {
                    switch (orRule.getHeader().getName()) {
                        case HEADER_NAME_AUTHORITY:
                            hosts.add(headerMatch2StringMatch(orRule.getHeader()));
                            break;
                        case HEADER_NAME_METHOD:
                            methods.add(headerMatch2StringMatch(orRule.getHeader()));
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
                hostList.add(hosts);
            }
            if (!ports.isEmpty()) {
                portList.add(ports);
            }
            if (!methods.isEmpty()) {
                methodList.add(methods);
            }
            if (!paths.isEmpty()) {
                pathList.add(paths);
            }
            if (!destIps.isEmpty()) {
                destIpList.add(destIps);
            }
        }
        TargetRuleManager.addTargetRules(new TargetRule(name, hostList, portList, methodList, pathList), isAllowed);
        IpBlockRuleManager.updateDestIpRules(name, destIpList, isAllowed);
    }

    private static StringMatcher headerMatch2StringMatch(HeaderMatcher headerMatcher) {
        if (!headerMatcher.hasStringMatch()) {
            StringMatcher.Builder builder = StringMatcher.newBuilder();
            builder.setExact(headerMatcher.getExactMatch())
                    .setContains(headerMatcher.getContainsMatch())
                    .setPrefix(headerMatcher.getPrefixMatch())
                    .setSuffix(headerMatcher.getSuffixMatch())
                    .setSafeRegex(headerMatcher.getSafeRegexMatch())
                    .setIgnoreCase(true);
            return builder.build();
        }
        return headerMatcher.getStringMatch();
    }
}
