package com.alibaba.cloud.istio.protocol;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public interface XdsProtocol<T> {
    List<T> getResource(Set<String> resourceNames);
    String getTypeUrl();
    long observeResource(Set<String> resourceNames, Consumer<List<T>> consumer);
}
