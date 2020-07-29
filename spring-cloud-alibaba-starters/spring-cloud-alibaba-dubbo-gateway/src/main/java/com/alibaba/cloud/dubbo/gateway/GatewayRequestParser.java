package com.alibaba.cloud.dubbo.gateway;



public interface GatewayRequestParser<T> {

    public String getMethod(T request);

    public String getHeader(T request,String key);

    public String getPath(T request);

    public String getParam(T request, String keyName);
}
