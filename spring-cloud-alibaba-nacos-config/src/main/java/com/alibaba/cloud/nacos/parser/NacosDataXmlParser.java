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
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.alibaba.nacos.client.utils.StringUtils;

/**
 * With relatively few usage scenarios, only simple parsing is performed to reduce jar
 * dependencies
 *
 * @author zkz
 */
public class NacosDataXmlParser extends AbstractNacosDataParser {

	public NacosDataXmlParser() {
		super("xml");
	}

	@Override
	protected Properties doParse(String data) throws IOException {
		if (StringUtils.isEmpty(data)) {
			return null;
		}
		Map<String, String> map = parseXml2Map(data);
		return this.generateProperties(this.reloadMap(map));
	}

	private Map<String, String> parseXml2Map(String xml) throws IOException {
		xml = xml.replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replaceAll("\\t", "");
		Map<String, String> map = new HashMap<>(32);
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = documentBuilder
					.parse(new InputSource(new StringReader(xml)));
			if (null == document) {
				return null;
			}
			parseNodeList(document.getChildNodes(), map, "");
		}
		catch (Exception e) {
			throw new IOException("The xml content parse error.", e.getCause());
		}
		return map;
	}

	private void parseNodeList(NodeList nodeList, Map<String, String> map,
			String parentKey) {
		if (nodeList == null || nodeList.getLength() < 1) {
			return;
		}
		parentKey = parentKey == null ? "" : parentKey;
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			String value = node.getNodeValue();
			value = value == null ? "" : value.trim();
			String name = node.getNodeName();
			name = name == null ? "" : name.trim();

			if (StringUtils.isEmpty(name)) {
				continue;
			}

			String key = StringUtils.isEmpty(parentKey) ? name : parentKey + DOT + name;
			NamedNodeMap nodeMap = node.getAttributes();
			parseNodeAttr(nodeMap, map, key);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
				parseNodeList(node.getChildNodes(), map, key);
				continue;
			}
			if (value.length() < 1) {
				continue;
			}
			map.put(parentKey, value);
		}
	}

	private void parseNodeAttr(NamedNodeMap nodeMap, Map<String, String> map,
			String parentKey) {
		if (null == nodeMap || nodeMap.getLength() < 1) {
			return;
		}
		for (int i = 0; i < nodeMap.getLength(); i++) {
			Node node = nodeMap.item(i);
			if (null == node) {
				continue;
			}
			if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
				if (StringUtils.isEmpty(node.getNodeName())) {
					continue;
				}
				if (StringUtils.isEmpty(node.getNodeValue())) {
					continue;
				}
				map.put(String.join(DOT, parentKey, node.getNodeName()),
						node.getNodeValue());
			}
		}
	}

}
