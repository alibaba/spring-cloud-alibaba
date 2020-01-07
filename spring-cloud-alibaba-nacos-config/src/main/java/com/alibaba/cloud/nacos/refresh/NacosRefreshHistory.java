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

package com.alibaba.cloud.nacos.refresh;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;

public class NacosRefreshHistory {

	private final static Logger log = LoggerFactory.getLogger(NacosRefreshHistory.class);

	private static final int MAX_SIZE = 20;

	private final LinkedList<Record> records = new LinkedList<>();

	private final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal
			.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

	private MessageDigest md;

	public NacosRefreshHistory() {
		try {
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			log.error("failed to initialize MessageDigest : ", e);
		}
	}

	/**
	 * recommend to use
	 * {@link NacosRefreshHistory#addRefreshRecord(java.lang.String, java.lang.String, java.lang.String)}.
	 * @param dataId dataId
	 * @param md5 md5
	 */
	@Deprecated
	public void add(String dataId, String md5) {
		records.addFirst(
				new Record(DATE_FORMAT.get().format(new Date()), dataId, "", md5, null));
		if (records.size() > MAX_SIZE) {
			records.removeLast();
		}
	}

	public void addRefreshRecord(String dataId, String group, String data) {
		records.addFirst(new Record(DATE_FORMAT.get().format(new Date()), dataId, group,
				md5(data), null));
		if (records.size() > MAX_SIZE) {
			records.removeLast();
		}
	}

	public LinkedList<Record> getRecords() {
		return records;
	}

	private String md5(String data) {
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		if (null == md) {
			try {
				md = MessageDigest.getInstance("MD5");
			}
			catch (NoSuchAlgorithmException ignored) {
				return "unable to get md5";
			}
		}
		return new BigInteger(1, md.digest(data.getBytes(StandardCharsets.UTF_8)))
				.toString(16);
	}

	static class Record {

		private final String timestamp;

		private final String dataId;

		private final String group;

		private final String md5;

		Record(String timestamp, String dataId, String group, String md5,
				Map<String, Object> last) {
			this.timestamp = timestamp;
			this.dataId = dataId;
			this.group = group;
			this.md5 = md5;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public String getDataId() {
			return dataId;
		}

		public String getGroup() {
			return group;
		}

		public String getMd5() {
			return md5;
		}

	}

}
