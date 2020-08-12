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

package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zkz
 */
public final class NacosDataParserHandler {

	private AbstractNacosDataParser parser;

	private NacosDataParserHandler() {
		parser = this.createParser();
	}

	/**
	 * Parsing nacos configuration content.
	 * @param data config from Nacos
	 * @param extension file extension. json or xml or yml or yaml or properties
	 * @return result of properties
	 * @throws IOException thrown if there is a problem parsing config.
	 */
	public Properties parseNacosData(String data, String extension) throws IOException {
		if (null == parser) {
			parser = this.createParser();
		}
		return parser.parseNacosData(data, extension);
	}

	/**
	 * check the validity of file extensions in dataid.
	 * @param dataIdAry array of dataId
	 * @return dataId handle success or not
	 */
	public boolean checkDataId(String... dataIdAry) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String dataId : dataIdAry) {
			int idx = dataId.lastIndexOf(AbstractNacosDataParser.DOT);
			if (idx > 0 && idx < dataId.length() - 1) {
				String extension = dataId.substring(idx + 1);
				if (parser.checkFileExtension(extension)) {
					continue;
				}
			}
			// add tips
			stringBuilder.append(dataId).append(",");
		}
		if (stringBuilder.length() > 0) {
			String result = stringBuilder.substring(0, stringBuilder.length() - 1);
			throw new IllegalStateException(AbstractNacosDataParser.getTips(result));
		}
		return true;
	}

	private AbstractNacosDataParser createParser() {
		return new NacosDataPropertiesParser().addNextParser(new NacosDataYamlParser())
				.addNextParser(new NacosDataXmlParser())
				.addNextParser(new NacosDataJsonParser());
	}

	public static NacosDataParserHandler getInstance() {
		return ParserHandler.HANDLER;
	}

	private static class ParserHandler {

		private static final NacosDataParserHandler HANDLER = new NacosDataParserHandler();

	}

}
