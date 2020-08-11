package com.alibaba.cloud.dubbospringcloudgatewayadapter.api;

public interface RequestParser<T> {

    public String requestMethod(T request);

    public String requestHeader(T request, String key);

    public String requestPath(T request);

    public String requestParams(T request, String keyName);
}
