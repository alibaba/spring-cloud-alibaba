package com.alibaba.cloud.dubbo.servicediscovery;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.registry.client.AbstractServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.apache.dubbo.registry.client.ServiceInstance;
import org.apache.dubbo.registry.client.event.listener.ServiceInstancesChangedListener;
import org.apache.dubbo.registry.nacos.NacosServiceDiscovery;

import java.util.Collections;
import java.util.Set;

/**
 * wrap a service Discovery
 */
public class SpringCloudServiceDiscovery extends AbstractServiceDiscovery {

    private AbstractServiceDiscovery abstractServiceDiscovery;

    /*
     * wrap and set real serviceDiscovery
     */
    public SpringCloudServiceDiscovery(){
//        abstractServiceDiscovery=new NacosServiceDiscovery();
    }


    @Override
    public void doRegister(ServiceInstance serviceInstance) {
//        abstractServiceDiscovery.doRegister(serviceInstance);
    }

    @Override
    public void doUpdate(ServiceInstance serviceInstance) {
//        abstractServiceDiscovery.update(serviceInstance);
    }

    @Override
    public void initialize(URL registryURL) throws Exception {
//        abstractServiceDiscovery.initialize(registryURL);
    }

    @Override
    public void destroy() throws Exception {
//        abstractServiceDiscovery.destroy();
    }

    @Override
    public void unregister(ServiceInstance serviceInstance) throws RuntimeException {
//        unregister(serviceInstance);
    }

    @Override
    public Set<String> getServices() {
//        return abstractServiceDiscovery.getServices();
        return Collections.emptySet();

    }

    @Override
    public URL getUrl() {
//        return abstractServiceDiscovery.getUrl();
        return new URL("","",0);
    }

    @Override
    public void addServiceInstancesChangedListener(ServiceInstancesChangedListener listener) throws NullPointerException, IllegalArgumentException {
//        abstractServiceDiscovery.addServiceInstancesChangedListener(listener);
    }
}
