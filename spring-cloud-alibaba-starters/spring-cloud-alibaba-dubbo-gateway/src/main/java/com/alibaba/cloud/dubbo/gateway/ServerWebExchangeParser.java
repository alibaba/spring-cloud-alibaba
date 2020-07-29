package com.alibaba.cloud.dubbo.gateway;

import org.springframework.web.server.ServerWebExchange;

public class ServerWebExchangeParser implements GatewayRequestParser<ServerWebExchange>{

    @Override
    public String getMethod(ServerWebExchange request) {
        return request.getRequest().getMethodValue();
    }

    @Override
    public String getHeader(ServerWebExchange request, String key) {
        return request.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String getPath(ServerWebExchange request) {
        return request.getRequest().getPath().value();
    }

    @Override
    public String getParam(ServerWebExchange request, String keyName) {
        return request.getRequest().getQueryParams().getFirst(keyName);
    }
}
