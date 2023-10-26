/*
 * Copyright 2022-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.mtls.server.netty;

import java.util.List;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSessionContext;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.ApplicationProtocolNegotiator;
import io.netty.handler.ssl.SslContext;

public class SslContextDelegate extends SslContext {

	private SslContext context;

	public SslContextDelegate(SslContext context) {
		this.context = context;
	}

	public void setContext(SslContext context) {
		this.context = context;
	}

	@Override
	public boolean isClient() {
		return context.isClient();
	}

	@Override
	public List<String> cipherSuites() {
		return context.cipherSuites();
	}

	@Override
	public ApplicationProtocolNegotiator applicationProtocolNegotiator() {
		return context.applicationProtocolNegotiator();
	}

	@Override
	public SSLEngine newEngine(ByteBufAllocator byteBufAllocator) {
		return context.newEngine(byteBufAllocator);
	}

	@Override
	public SSLEngine newEngine(ByteBufAllocator byteBufAllocator, String s, int i) {
		return context.newEngine(byteBufAllocator, s, i);
	}

	@Override
	public SSLSessionContext sessionContext() {
		return context.sessionContext();
	}

}
