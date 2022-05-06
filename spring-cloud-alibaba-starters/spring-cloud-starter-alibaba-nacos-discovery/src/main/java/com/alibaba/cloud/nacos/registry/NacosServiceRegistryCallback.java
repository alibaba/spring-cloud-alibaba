package com.alibaba.cloud.nacos.registry;

/**
 * @author changjin wei(魏昌进)
 * @since 2022/5/6
 */
public interface NacosServiceRegistryCallback {

    /**
     * Nacos Service Registry finished Callback
     */
    void success();

    /**
     * Nacos Service Registry fail Callback
     */
    void fail();
}
