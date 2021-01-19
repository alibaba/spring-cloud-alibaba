/*
 * Copyright 2013-2019 the original author or authors.
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

package com.alibaba.cloud.commons;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * FileUtils. copy from apache commons.io.
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class FileUtils {

	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 *
	 * @param file the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the file contents, never {@code null}
	 * @throws java.io.IOException in case of an I/O error
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of
	 *     {@link java.io .UnsupportedEncodingException} in version 2.2 if the encoding is
	 *     not supported.
	 * @since 2.3
	 */
	public static String readFileToString(final File file, final String encoding)
			throws IOException {
		return readFileToString(file, Charsets.toCharset(encoding));
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for the VM.
	 * The file is always closed.
	 *
	 * @param file the file to read, must not be {@code null}
	 * @return the file contents, never {@code null}
	 * @throws IOException in case of an I/O error
	 * @since 1.3.1
	 * @deprecated 2.5 use {@link #readFileToString(File, java.nio.charset.Charset)}
	 * instead (and specify the appropriate encoding)
	 */
	@Deprecated
	public static String readFileToString(final File file) throws IOException {
		return readFileToString(file, Charset.defaultCharset());
	}
}
