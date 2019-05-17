package org.springframework.cloud.alibaba.nacos.ribbon;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

import java.util.Map;

public class NacosServerIntrospector extends DefaultServerIntrospector {

	@Override
	public Map<String, String> getMetadata(Server server) {
		if (server instanceof NacosServer) {
			NacosServer nacosServer = (NacosServer) server;
			return nacosServer.getMetadata();
		}
		return super.getMetadata(server);
	}

}
