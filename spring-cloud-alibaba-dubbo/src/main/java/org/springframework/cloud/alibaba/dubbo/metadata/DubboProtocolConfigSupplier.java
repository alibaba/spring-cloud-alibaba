/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.metadata;

import org.apache.dubbo.config.ProtocolConfig;

import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;

import static org.apache.dubbo.common.Constants.DEFAULT_PROTOCOL;

/**
 * Dubbo's {@link ProtocolConfig} {@link Supplier}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboProtocolConfigSupplier implements Supplier<ProtocolConfig> {

    private final ObjectProvider<Collection<ProtocolConfig>> protocols;

    public DubboProtocolConfigSupplier(ObjectProvider<Collection<ProtocolConfig>> protocols) {
        this.protocols = protocols;
    }

    @Override
    public ProtocolConfig get() {
        ProtocolConfig protocolConfig = null;
        Collection<ProtocolConfig> protocols = this.protocols.getIfAvailable();
        for (ProtocolConfig protocol : protocols) {
            String protocolName = protocol.getName();
            if (DEFAULT_PROTOCOL.equals(protocolName)) {
                protocolConfig = protocol;
                break;
            }
        }

        if (protocolConfig == null) { // If The ProtocolConfig bean named "dubbo" is absent, take first one of them
            Iterator<ProtocolConfig> iterator = protocols.iterator();
            protocolConfig = iterator.hasNext() ? iterator.next() : null;
        }

        if (protocolConfig == null) {
            protocolConfig = new ProtocolConfig();
            protocolConfig.setName(DEFAULT_PROTOCOL);
            protocolConfig.setPort(20880);
        }

        return protocolConfig;
    }
}
