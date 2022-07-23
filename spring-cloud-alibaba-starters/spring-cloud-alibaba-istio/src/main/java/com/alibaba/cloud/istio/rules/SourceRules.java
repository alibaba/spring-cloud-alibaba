package com.alibaba.cloud.istio.rules;

import io.envoyproxy.envoy.config.core.v3.CidrRange;
import io.envoyproxy.envoy.config.rbac.v3.Principal;
import io.envoyproxy.envoy.config.route.v3.HeaderMatcher;
import io.envoyproxy.envoy.type.matcher.v3.ListMatcher;
import io.envoyproxy.envoy.type.matcher.v3.MetadataMatcher;
import io.envoyproxy.envoy.type.matcher.v3.RegexMatcher;
import io.envoyproxy.envoy.type.matcher.v3.StringMatcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceRules {
    private final List<List<StringMatcher>> PRINCIPALS = new ArrayList<>();
    private final List<List<StringMatcher>> REQUEST_PRINCIPALS = new ArrayList<>();
    private final List<List<StringMatcher>> AUTH_AUDIENCE = new ArrayList<>();
    private final List<List<StringMatcher>> AUTH_PRESENTER = new ArrayList<>();
    private final Map<String, List<List<ListMatcher>>> AUTH_CLAIMS = new HashMap<>();
    private final List<List<RegexMatcher>> NAMESPACES = new ArrayList<>();
    private final List<List<CidrRange>> IP_BLOCKS = new ArrayList<>();
    private final List<List<CidrRange>> REMOTE_IP_BLOCKS = new ArrayList<>();
    private final Map<String, List<List<HeaderMatcher>>> HEADERS = new HashMap<>();

    private static final String ISTIO_AUTHN = "istio_authn";
    private static final String REQUEST_AUTH_PRINCIPAL = "request.auth.principal";
    private static final String REQUEST_AUTH_AUDIENCE = "request.auth.audiences";
    private static final String REQUEST_AUTH_PRESENTER = "request.auth.presenter";
    private static final String REQUEST_AUTH_CLAIMS = "request.auth.claims";

    private final String NAME;

    public String getNAME() {
        return NAME;
    }

    @Override
    public String toString() {
        return "SourceRules{" +
                "PRINCIPALS=" + PRINCIPALS +
                ", REQUEST_PRINCIPALS=" + REQUEST_PRINCIPALS +
                ", AUTH_AUDIENCE=" + AUTH_AUDIENCE +
                ", AUTH_PRESENTER=" + AUTH_PRESENTER +
                ", AUTH_CLAIMS=" + AUTH_CLAIMS +
                ", NAMESPACES=" + NAMESPACES +
                ", IP_BLOCKS=" + IP_BLOCKS +
                ", REMOTE_IP_BLOCKS=" + REMOTE_IP_BLOCKS +
                ", NAME='" + NAME + '\'' +
                '}';
    }

    public SourceRules(String name, Principal principal, boolean isAllowed) {
        this.NAME = name;
        if (isAllowed) {
            principal = principal.getNotId();
        }
        Principal.Set andIds = principal.getAndIds();
        for (Principal andId : andIds.getIdsList()) {
            Principal.Set orIds = andId.getOrIds();
            List<StringMatcher> principals = new ArrayList<>();
            List<RegexMatcher> namespaces = new ArrayList<>();
            List<CidrRange> ipBlocks = new ArrayList<>();
            List<CidrRange> remoteIpBlocks = new ArrayList<>();
            List<StringMatcher> requestPrincipals = new ArrayList<>();
            List<StringMatcher> authAudiences = new ArrayList<>();
            List<StringMatcher> authPresenters = new ArrayList<>();
            Map<String, List<ListMatcher>> authClaims = new HashMap<>();
            Map<String, List<HeaderMatcher>> headers = new HashMap<>();
            for (Principal orId : orIds.getIdsList()) {
                if (orId.hasAuthenticated() && orId.getAuthenticated().hasPrincipalName()) {
                    if (orId.getAuthenticated().getPrincipalName().hasSafeRegex()) {
                        namespaces.add(orId.getAuthenticated().getPrincipalName().getSafeRegex());
                    } else {
                        principals.add(orId.getAuthenticated().getPrincipalName());
                    }
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
                    if (orId.hasHeader()) {
                        List<HeaderMatcher> headerMatchers = headers.getOrDefault(orId.getHeader().getName(), new ArrayList<>());
                        headerMatchers.add(orId.getHeader());
                        headers.put(orId.getHeader().getName(), headerMatchers);
                    }
                }
                PRINCIPALS.add(principals);
                REQUEST_PRINCIPALS.add(requestPrincipals);
                AUTH_AUDIENCE.add(authAudiences);
                AUTH_PRESENTER.add(authPresenters);
                NAMESPACES.add(namespaces);
                IP_BLOCKS.add(ipBlocks);
                REMOTE_IP_BLOCKS.add(remoteIpBlocks);
                for (Map.Entry<String, List<ListMatcher>> entry : authClaims.entrySet()) {
                    List<List<ListMatcher>> authClaimEntries = AUTH_CLAIMS.getOrDefault(entry.getKey(), new ArrayList<>());
                    authClaimEntries.add(entry.getValue());
                    AUTH_CLAIMS.put(entry.getKey(), authClaimEntries);
                }
                for (Map.Entry<String, List<HeaderMatcher>> entry : headers.entrySet()) {
                    List<List<HeaderMatcher>> headerEntries = HEADERS.getOrDefault(entry.getKey(), new ArrayList<>());
                    headerEntries.add(entry.getValue());
                    HEADERS.put(entry.getKey(), headerEntries);
                }
            }
        }
    }

}
