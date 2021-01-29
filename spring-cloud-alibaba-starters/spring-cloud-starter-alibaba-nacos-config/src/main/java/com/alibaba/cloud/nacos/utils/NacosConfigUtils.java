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

/**
 * @author zkzlx
 */
public final class NacosConfigUtils {

	private NacosConfigUtils() {
	}

	/**
	 * Convert Chinese characters to Unicode.
	 * @param configValue value of config
	 * @return new string
	 */
	public static String selectiveConvertUnicode(String configValue) {
		StringBuilder sb = new StringBuilder();
		char[] chars = configValue.toCharArray();
		for (char aChar : chars) {
			if (isBaseLetter(aChar)) {
				sb.append(aChar);
			}
			else {
				sb.append(String.format("\\u%04x", (int) aChar));
			}
		}
		return sb.toString();
	}

	/**
	 * char is base latin or whitespace?
	 * @param ch a character
	 * @return true or false
	 */
	public static boolean isBaseLetter(char ch) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(ch);
		return ub == Character.UnicodeBlock.BASIC_LATIN || Character.isWhitespace(ch);
	}

	/**
	 * char is chinese?
	 * @param c a character
	 * @return true or false
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
	}

}
