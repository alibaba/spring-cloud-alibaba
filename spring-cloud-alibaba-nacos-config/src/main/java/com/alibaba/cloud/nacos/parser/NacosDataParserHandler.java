package com.alibaba.cloud.nacos.parser;

import java.io.IOException;
import java.util.Properties;

/**
 * @Author: zkz
 */
public class NacosDataParserHandler {

	private static final NacosDataParserHandler HANDLER = new NacosDataParserHandler();

	private AbstractNacosDataParser parser;

	private NacosDataParserHandler() {
		parser = this.createParser();
	}

	/** Parsing nacos configuration content */
	public Properties parseNacosData(String data, String extension) throws IOException {
		if (null == parser) {
			parser = this.createParser();
		}
		return parser.parseNacosData(data, extension);
	}

	/** check the validity of file extensions in dataid */
	public boolean checkDataId(String... dataIdAry) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String dataId : dataIdAry) {
			int idx = dataId.lastIndexOf(AbstractNacosDataParser.DOT);
			if (idx > 0 && idx < dataId.length() - 1) {
				String extension = dataId.substring(idx + 1);
				if (parser.checkFileExtension(extension)) {
					break;
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
		return HANDLER;
	}

}
