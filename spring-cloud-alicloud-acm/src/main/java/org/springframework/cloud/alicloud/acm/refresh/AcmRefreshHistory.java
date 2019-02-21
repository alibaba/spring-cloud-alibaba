/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.acm.refresh;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author juven.xuxb, 5/16/16.
 */
public class AcmRefreshHistory {

	private static final int MAX_SIZE = 20;

	private LinkedList<Record> records = new LinkedList<>();

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void add(String dataId, String md5) {
		records.addFirst(new Record(dateFormat.format(new Date()), dataId, md5));
		if (records.size() > MAX_SIZE) {
			records.removeLast();
		}
	}

	public LinkedList<Record> getRecords() {
		return records;
	}
}

class Record {

	private final String timestamp;

	private final String dataId;

	private final String md5;

	public Record(String timestamp, String dataId, String md5) {
		this.timestamp = timestamp;
		this.dataId = dataId;
		this.md5 = md5;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getDataId() {
		return dataId;
	}

	public String getMd5() {
		return md5;
	}
}
