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

package com.alibaba.cloud.nacos.parser;

import org.springframework.core.io.ByteArrayResource;

/**
 * Nacos-specific resource.
 *
 * @author zkz
 */
public class NacosByteArrayResource extends ByteArrayResource {

	private String filename;

	/**
	 * Create a new {@code ByteArrayResource}.
	 * @param byteArray the byte array to wrap
	 */
	public NacosByteArrayResource(byte[] byteArray) {
		super(byteArray);
	}

	/**
	 * Create a new {@code ByteArrayResource} with a description.
	 * @param byteArray the byte array to wrap
	 * @param description where the byte array comes from
	 */
	public NacosByteArrayResource(byte[] byteArray, String description) {
		super(byteArray, description);
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * This implementation always returns {@code null}, assuming that this resource type
	 * does not have a filename.
	 */
	@Override
	public String getFilename() {
		return null == this.filename ? this.getDescription() : this.filename;
	}

}
