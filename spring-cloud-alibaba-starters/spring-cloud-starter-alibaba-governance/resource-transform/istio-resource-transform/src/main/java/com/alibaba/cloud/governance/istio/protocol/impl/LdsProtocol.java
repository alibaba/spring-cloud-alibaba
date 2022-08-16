package com.alibaba.cloud.governance.istio.protocol.impl;


import com.alibaba.cloud.governance.istio.XdsChannel;
import com.alibaba.cloud.governance.istio.XdsConfigProperties;
import com.alibaba.cloud.governance.istio.protocol.AbstractXdsProtocol;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;

import java.util.ArrayList;
import java.util.List;

public class LdsProtocol extends AbstractXdsProtocol<Listener> {


    public LdsProtocol(XdsChannel xdsChannel, XdsConfigProperties xdsConfigProperties) {
        super(xdsChannel, xdsConfigProperties);
    }

    @Override
    public String getTypeUrl() {
        return "type.googleapis.com/envoy.config.listener.v3.Listener";
    }

    @Override
    public List<Listener> decodeXdsResponse(DiscoveryResponse response) {
        List<Listener> listeners = new ArrayList<Listener>();
        for (com.google.protobuf.Any res : response.getResourcesList()) {
            try {
                Listener listener = res.unpack(Listener.class);
                listeners.add(listener);
            } catch (Exception e) {
                // TODO: print with slf4j
                e.printStackTrace();
            }
        }
        return listeners;
    }

}
