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

package com.alibaba.cloud.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * FileUtils. copy from apache commons-io.
 *
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public final class FileUtils {

	private FileUtils() {
	}

	// -----------------------------------------------------------------------
	/**
	 * Opens a {@link java.io.FileInputStream} for the specified file, providing better
	 * error messages than simply calling <code>new FileInputStream(file)</code>.
	 * <p>
	 * At the end of the method either the stream will be successfully opened, or an
	 * exception will have been thrown.
	 * <p>
	 * An exception is thrown if the file does not exist. An exception is thrown if the
	 * file object exists but is a directory. An exception is thrown if the file exists
	 * but cannot be read.
	 * @param file the file to open for input, must not be {@code null}
	 * @return a new {@link java.io.FileInputStream} for the specified file
	 * @throws java.io.FileNotFoundException if the file does not exist
	 * @throws IOException if the file object is a directory
	 * @throws IOException if the file cannot be read
	 * @since 1.3
	 */
	public static FileInputStream openInputStream(final File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (!file.canRead()) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		}
		else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}

	// -----------------------------------------------------------------------
	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 * @param file the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the file contents, never {@code null}
	 * @throws IOException in case of an I/O error
	 */
	public static String readFileToString(final File file, final Charset encoding)
			throws IOException {
		try (InputStream in = openInputStream(file)) {
			return IOUtils.toString(in, Charsets.toCharset(encoding));
		}
	}

	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 * @param file the file to read, must not be {@code null}
	 * @param encoding the encoding to use, {@code null} means platform default
	 * @return the file contents, never {@code null}
	 * @throws java.io.IOException in case of an I/O error
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of
	 * {@link java.io .UnsupportedEncodingException} in version 2.2 if the encoding is not
	 * supported.
	 */
	public static String readFileToString(final File file, final String encoding)
			throws IOException {
		return readFileToString(file, Charsets.toCharset(encoding));
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for the VM.
	 * The file is always closed.
	 * @param file the file to read, must not be {@code null}
	 * @return the file contents, never {@code null}
	 * @throws IOException in case of an I/O error
	 * @deprecated 2.5 use {@link #readFileToString(File, String)} instead (and specify
	 * the appropriate encoding)
	 */
	@Deprecated
	public static String readFileToString(final File file) throws IOException {
		return readFileToString(file, Charset.defaultCharset());
	}

}
