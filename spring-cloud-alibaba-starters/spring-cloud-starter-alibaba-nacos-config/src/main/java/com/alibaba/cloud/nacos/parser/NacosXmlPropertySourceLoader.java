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

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * Parsing for XML requires overwriting the default
 * {@link PropertiesPropertySourceLoader}, because it internally rigorously validates
 * ({@conde DOCTYPE}) THE XML in a way that makes it difficult to customize the
 * configuration; at finally, make sure it's in the first place.
 *
 * @author zkz
 */
public class NacosXmlPropertySourceLoader extends AbstractPropertySourceLoader
		implements Ordered {

	/**
	 * Get the order value of this object.
	 * <p>
	 * Higher values are interpreted as lower priority. As a consequence, the object with
	 * the lowest value has the highest priority (somewhat analogous to Servlet
	 * {@code load-on-startup} values).
	 * <p>
	 * Same order values will result in arbitrary sort positions for the affected objects.
	 * @return the order value
	 * @see #HIGHEST_PRECEDENCE
	 * @see #LOWEST_PRECEDENCE
	 */
	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

	/**
	 * Returns the file extensions that the loader supports (excluding the '.').
	 * @return the file extensions
	 */
	@Override
	public String[] getFileExtensions() {
		return new String[] { "xml" };
	}

	/**
	 * Load the resource into one or more property sources. Implementations may either
	 * return a list containing a single source, or in the case of a multi-document format
	 * such as yaml a source for each document in the resource.
	 * @param name the root name of the property source. If multiple documents are loaded
	 * an additional suffix should be added to the name for each source loaded.
	 * @param resource the resource to load
	 * @return a list property sources
	 * @throws IOException if the source cannot be loaded
	 */
	@Override
	protected List<PropertySource<?>> doLoad(String name, Resource resource)
			throws IOException {
		Map<String, Object> nacosDataMap = parseXml2Map(resource);
		return Collections.singletonList(
				new OriginTrackedMapPropertySource(name, nacosDataMap, true));

	}

	private Map<String, Object> parseXml2Map(Resource resource) throws IOException {
		Map<String, Object> map = new LinkedHashMap<>(32);
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = documentBuilder.parse(resource.getInputStream());
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

	private void parseNodeList(NodeList nodeList, Map<String, Object> map,
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

	private void parseNodeAttr(NamedNodeMap nodeMap, Map<String, Object> map,
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
