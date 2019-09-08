package com.alibaba.cloud.dubbo.metadata.repository;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;
import java.util.Optional;

/**
 * metadata service instance selector
 *
 * @author liuxx
 * @date 2019/9/4
 */
public interface MetadataServiceInstanceSelector {


    /**
     * choose a service instance to get metadata
     * @param serviceInstances all service instance
     * @return the service instance to get metadata
     */
    Optional<ServiceInstance> choose(List<ServiceInstance> serviceInstances);
}
