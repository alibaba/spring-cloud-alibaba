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

package com.alibaba.cloud.governance.common.matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpMatcher {

	private int prefixLen;

	private String ip;

	public IpMatcher() {

	}

	public IpMatcher(int prefixLen, String ip) {
		this.prefixLen = prefixLen;
		this.ip = ip;
	}

	public int getPrefixLen() {
		return prefixLen;
	}

	public String getIp() {
		return ip;
	}

	private static final Logger log = LoggerFactory.getLogger(IpMatcher.class);

	public boolean match(String ip) {
		String ruleIp = ip2BinaryString(this.ip);
		if (StringUtils.isEmpty(ruleIp)) {
			return false;
		}
		String ipBinary = ip2BinaryString(ip);
		if (StringUtils.isEmpty(ipBinary)) {
			return false;
		}
		if (prefixLen <= 0) {
			return ruleIp.equals(ipBinary);
		}
		if (ruleIp.length() >= prefixLen && ipBinary.length() >= prefixLen) {
			return ruleIp.substring(0, prefixLen)
					.equals(ipBinary.substring(0, prefixLen));
		}
		return false;
	}

	/**
	 * @param ip dotted ip string, for example: 127.0.0.1
	 * @return
	 */
	private String ip2BinaryString(String ip) {
		try {
			String[] ips = ip.split("\\.");
			long[] ipLong = new long[4];
			for (int i = 0; i < 4; ++i) {
				ipLong[i] = Long.parseLong(ips[i]);
				if (ipLong[i] < 0 || ipLong[i] > 255) {
					return "";
				}
			}
			return String
					.format("%32s", Long.toBinaryString((ipLong[0] << 24)
							+ (ipLong[1] << 16) + (ipLong[2] << 8) + ipLong[3]))
					.replace(" ", "0");
		}
		catch (Exception e) {
			log.error("failed to parse ip {} to binary string", ip);
		}
		return "";
	}

}
