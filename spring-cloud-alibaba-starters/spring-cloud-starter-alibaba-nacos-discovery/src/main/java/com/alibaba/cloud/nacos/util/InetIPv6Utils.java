/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.util;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.List;

import com.alibaba.cloud.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;

/**
 * @author HH
 */
public class InetIPv6Utils {

	private final static Log log = LogFactory.getLog(InetIPv6Utils.class);

	private final InetUtilsProperties properties;


	public InetIPv6Utils(final InetUtilsProperties properties) {
		this.properties = properties;
	}

	private InetUtils.HostInfo findFirstValidHostInfo() {
		InetAddress address = this.findFirstValidIPv6Address();
		return address != null ? this.getHostInfo(address) : null;
	}

	private InetAddress findFirstValidIPv6Address() {
		InetAddress address = null;

		try {
			for (Enumeration<NetworkInterface> nics = NetworkInterface
					.getNetworkInterfaces(); nics.hasMoreElements(); ) {
				NetworkInterface ifc = nics.nextElement();
				if (ifc.isUp() || !ifc.isVirtual() || !ifc.isLoopback()) {
					if (address != null) {
						break;
					}

					if (!ignoreInterface(ifc.getDisplayName())) {
						for (Enumeration<InetAddress> addrs = ifc
								.getInetAddresses(); addrs.hasMoreElements(); ) {
							InetAddress inetAddress = addrs.nextElement();
							if (inetAddress instanceof Inet6Address
									// filter ::1
									&& !inetAddress.isLoopbackAddress()
									// filter fe80::/10
									&& !inetAddress.isLinkLocalAddress()
									// filter ::/128
									&& !inetAddress.isAnyLocalAddress()
									// filter fec0::/10,which was discarded, but some
									// address may be deployed.
									&& !inetAddress.isSiteLocalAddress()
									// filter fd00::/8
									&& !isUniqueLocalAddress(inetAddress)
									&& isPreferredAddress(inetAddress)) {
								log.trace("Found non-loopback interface: "
										+ ifc.getDisplayName());
								address = inetAddress;
								break;
							}
						}
					}
				}
			}
		}
		catch (IOException e) {
			log.error("Cannot get first non-loopback address", e);
		}
		return address;
	}

	public String findIPv6Address() {
		InetUtils.HostInfo hostInfo = findFirstValidHostInfo();
		return hostInfo != null ? normalizeIPv6(hostInfo.getIpAddress()) : null;
	}

	private String normalizeIPv6(String ip) {
		// Remove the suffix of network card in IPv6 address, such as
		// 2408:400a:8c:5400:6578:5c42:77b1:bc5d%eth0
		int idx = ip.indexOf("%");
		return idx != -1 ? "[" + ip.substring(0, idx) + "]" : "[" + ip + "]";
	}

	private boolean isPreferredAddress(InetAddress address) {
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
		return false;
	}

	boolean ignoreInterface(String interfaceName) {
		for (String regex : this.properties.getIgnoredInterfaces()) {
			if (interfaceName.matches(regex)) {
				return true;
			}
		}
		return false;
	}

	private InetUtils.HostInfo getHostInfo(final InetAddress address) {
		InetUtils.HostInfo hostInfo = new InetUtils.HostInfo();
		String hostName = address.getHostName();
		if (hostName == null) {
			hostName = "localhost";
		}
		hostInfo.setHostname(hostName);
		if (StringUtils.isNotEmpty(address.getHostAddress())) {
			hostInfo.setIpAddress(address.getHostAddress());
		}
		else {
			hostInfo.setIpAddress(StringUtils.EMPTY);
		}
		return hostInfo;
	}

	/**
	 * If the address is Unique Local Address.
	 *
	 * @param inetAddress {@link InetAddress}
	 * @return {@code true} if the address is Unique Local Address, otherwise {@code false}
	 */
	private boolean isUniqueLocalAddress(InetAddress inetAddress) {
		byte[] ip = inetAddress.getAddress();
		return (ip[0] & 0xff) == 0xfd;
	}

}
