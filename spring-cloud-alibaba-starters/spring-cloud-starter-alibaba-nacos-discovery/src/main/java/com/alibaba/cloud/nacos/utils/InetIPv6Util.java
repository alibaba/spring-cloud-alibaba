/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.nacos.utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

/**
 * @author HH
 */
public class InetIPv6Util implements Closeable {

	private final ExecutorService executorService;

	private final Log log = LogFactory.getLog(InetIPv6Util.class);

	private final InetUtilsProperties properties;

	@Override
	public void close() {
		this.executorService.shutdown();
	}

	public InetIPv6Util(final InetUtilsProperties properties) {
		this.properties = properties;
		this.executorService = Executors.newSingleThreadExecutor((r) -> {
			Thread thread = new Thread(r);
			thread.setName("spring.cloud.alibaba.inetIPV6Util");
			thread.setDaemon(true);
			return thread;
		});
	}

	public InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
		InetAddress address = this.findFirstNonLoopbackIPv6Address();
		if (address != null) {
			return this.convertAddress(address);
		}
		return null;
	}

	public InetAddress findFirstNonLoopbackIPv6Address() {
		InetAddress address = null;

		try {
			int lowest = Integer.MAX_VALUE;
			for (Enumeration<NetworkInterface> nics = NetworkInterface
					.getNetworkInterfaces(); nics.hasMoreElements();) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp()) {
					log.trace("Testing interface:" + ifc.getDisplayName());
					if (ifc.getIndex() < lowest || address == null) {
						lowest = ifc.getIndex();
					}
					else if (address != null) {
						continue;
					}

					if (!ignoreInterface(ifc.getDisplayName())) {
						for (Enumeration<InetAddress> addrs = ifc
								.getInetAddresses(); addrs.hasMoreElements();) {
							InetAddress inetAddress = addrs.nextElement();
							if (inetAddress instanceof Inet6Address
									&& !inetAddress.isLoopbackAddress()
									&& isPreferredAddress(inetAddress)) {
								log.trace("Found non-loopback interface: "
										+ ifc.getDisplayName());
								address = inetAddress;
							}
						}
					}
				}
			}
		}
		catch (IOException e) {
			log.error("Cannot get first non-loopback address", e);
		}
		if (address == null) {
			try {
				InetAddress localHost = InetAddress.getLocalHost();
				if (localHost instanceof Inet6Address && !localHost.isLoopbackAddress()
						&& isPreferredAddress(localHost)) {
					address = localHost;
				}
			}
			catch (UnknownHostException e) {
				log.warn("Unable to retrieve localhost");
			}
		}
		return address;
	}

	public String findIPv6Address() {
		InetUtils.HostInfo hostInfo = findFirstNonLoopbackHostInfo();
		String ip = hostInfo != null ? hostInfo.getIpAddress() : "";
		if (!StringUtils.isEmpty(ip)) {
			int index = ip.indexOf('%');
			ip = index > 0 ? ip.substring(0, index) : ip;
			return iPv6Format(ip);
		}
		return ip;
	}

	public String iPv6Format(String ip) {
		return "[" + ip + "]";
	}

	boolean isPreferredAddress(InetAddress address) {
		if (this.properties.isUseOnlySiteLocalInterfaces()) {
			final boolean siteLocalAddress = address.isSiteLocalAddress();
			if (!siteLocalAddress) {
				log.trace("Ignoring address" + address.getHostAddress());
			}
			return siteLocalAddress;
		}
		final List<String> preferredNetworks = this.properties.getPreferredNetworks();
		if (preferredNetworks.isEmpty()) {
			return true;
		}
		for (String regex : preferredNetworks) {
			final String hostAddress = address.getHostAddress();
			if (hostAddress.matches(regex) || hostAddress.startsWith(regex)) {
				return true;
			}
		}
		log.trace("Ignoring address: " + address.getHostAddress());
		return false;
	}

	boolean ignoreInterface(String interfaceName) {
		for (String regex : this.properties.getIgnoredInterfaces()) {
			if (interfaceName.matches(regex)) {
				log.trace("Ignoring interface: " + interfaceName);
				return true;
			}
		}
		return false;
	}

	public InetUtils.HostInfo convertAddress(final InetAddress address) {
		InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
		Future<String> result = this.executorService.submit(address::getHostName);

		String hostname;
		try {
			hostname = result.get(this.properties.getTimeoutSeconds(), TimeUnit.SECONDS);
		}
		catch (Exception e) {
			log.info("Cannot determine local hostname");
			hostname = "localhost";
		}
		hostInfo.setHostname(hostname);
		hostInfo.setIpAddress(address.getHostAddress());
		return hostInfo;
	}

}
