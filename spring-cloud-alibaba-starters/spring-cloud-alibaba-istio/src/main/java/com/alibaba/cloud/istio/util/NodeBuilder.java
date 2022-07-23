package com.alibaba.cloud.istio.util;

import io.envoyproxy.envoy.config.core.v3.Node;

public class NodeBuilder {
    private final static String SVC_CLUSTER_LOCAL = ".svc.cluster.local";
    private static Node NODE;
    public static Node getNode() {
        if (NODE != null) {
            return NODE;
        }
        String podName = System.getenv("POD_NAME");
        String podNamespace = System.getenv("NAMESPACE_NAME");
        String svcName = System.getenv("SVC_NAME");
        return NODE = Node.newBuilder().setId(podName + "~" + podNamespace + SVC_CLUSTER_LOCAL).setCluster(svcName).build();
    }
}
