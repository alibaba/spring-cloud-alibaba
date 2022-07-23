package com.alibaba.cloud.istio.util;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import io.envoyproxy.envoy.config.listener.v3.FilterChain;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.rbac.v3.RBAC;
import io.envoyproxy.envoy.extensions.filters.network.http_connection_manager.v3.HttpConnectionManager;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListenerResolver {
    private static final String VIRTUAL_INBOUND = "virtualInbound";
    private static final String CONNECTION_MANAGER = "envoy.filters.network.http_connection_manager";
    private static final String RBAC_FILTER = "envoy.filters.http.rbac";

    public static List<RBAC> resolveRbac(List<Listener> listeners) {
        List<RBAC> rbacList = listeners.stream()
                .filter(listener -> listener.getName().equals(VIRTUAL_INBOUND))
                .map(Listener::getFilterChainsList)
                .flatMap(filterChains -> filterChains.stream().map(FilterChain::getFiltersList))
                .flatMap(filters -> filters.stream().filter(filter -> filter.getName().equals(CONNECTION_MANAGER)))
                .map(filter -> unpackHttpConnectionManager(filter.getTypedConfig()))
                .filter(Objects::nonNull)
                .flatMap(httpConnectionManager -> httpConnectionManager.getHttpFiltersList().stream())
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
        return rbacList;
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
}
