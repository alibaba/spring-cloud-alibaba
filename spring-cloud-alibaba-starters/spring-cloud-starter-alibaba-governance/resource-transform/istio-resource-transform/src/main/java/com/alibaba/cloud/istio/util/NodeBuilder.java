package com.alibaba.cloud.istio.util;

import io.envoyproxy.envoy.config.core.v3.Node;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeBuilder {
    private static final String SVC_CLUSTER_LOCAL = ".svc.cluster.local";
    private static final String DEFAULT_POD_NAME = "sidecar";
    private static final String DEFAULT_NAMESPACE = "default";
    private static final String DEFAULT_SVC_NAME = "app";
    private static Node NODE;
    public static Node getNode() {
        if (NODE != null) {
            return NODE;
        }
        String podName = System.getenv("POD_NAME");
        if (podName == null) {
            podName = DEFAULT_POD_NAME;
        }
        String podNamespace = System.getenv("NAMESPACE_NAME");
        if (podNamespace == null) {
            podNamespace = DEFAULT_NAMESPACE;
        }
        String svcName = System.getenv("SVC_NAME");
        if (svcName == null) {
            svcName = DEFAULT_SVC_NAME;
        }
        String ip = System.getenv("HOST_IP");
        String hostName = System.getenv("HOST_NAME");
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (ip == null) {
                ip = local.getHostAddress();
            }
            if (hostName == null) {
                hostName = local.getHostName();
            }
        } catch (UnknownHostException e) {

        }
        return Node.newBuilder().setId(String.format("%s~%s~%s.%s~%s" + SVC_CLUSTER_LOCAL, podName, ip, hostName, podNamespace, podNamespace)).build();
    }
}
