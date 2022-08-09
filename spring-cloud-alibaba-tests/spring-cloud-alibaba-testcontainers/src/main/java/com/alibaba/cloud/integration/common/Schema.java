/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alibaba.cloud.integration.common;

/**
 * Message schema definition.
 */
public interface Schema<T> extends Cloneable {

	/**
	 * Check if the message is a valid object for this schema.
	 *
	 * <p>
	 * The implementation can choose what its most efficient approach to validate the
	 * schema. If the implementation doesn't provide it, it will attempt to use
	 * {@link #decode(byte[])} to see if this schema can decode this message or not as a
	 * validation mechanism to verify the bytes.
	 *
	 * @param message the messages to verify
	 * @throws SchemaSerializationException if it is not a valid message
	 */
	default void validate(byte[] message) {
		decode(message);
	}

	/**
	 * Encode an object representing the message content into a byte array.
	 *
	 * @param message the message object
	 * @return a byte array with the serialized content
	 * @throws SchemaSerializationException if the serialization fails
	 */
	byte[] encode(T message);

	/**
	 * Returns whether this schema supports versioning.
	 *
	 * <p>
	 * Most of the schema implementations don't really support schema versioning, or it
	 * just doesn't make any sense to support schema versionings (e.g. primitive schemas).
	 * Only schema returns {@link GenericRecord} should support schema versioning.
	 *
	 * <p>
	 * If a schema implementation returns <tt>false</tt>, it should implement
	 * {@link #decode(byte[])}; while a schema implementation returns <tt>true</tt>, it
	 * should implement {@link #decode(byte[], byte[])} instead.
	 *
	 * @return true if this schema implementation supports schema versioning; otherwise
	 * returns false.
	 */
	default boolean supportSchemaVersioning() {
		return false;
	}

	/**
	 * Decode a byte array into an object using the schema definition and deserializer
	 * implementation.
	 *
	 * @param bytes the byte array to decode
	 * @return the deserialized object
	 */
	default T decode(byte[] bytes) {
		// use `null` to indicate ignoring schema version
		return decode(bytes, null);
	}

	/**
	 * Decode a byte array into an object using a given version.
	 *
	 * @param bytes the byte array to decode
	 * @param schemaVersion the schema version to decode the object. null indicates using
	 *     latest version.
	 * @return the deserialized object
	 */
	default T decode(byte[] bytes, byte[] schemaVersion) {
		// ignore version by default (most of the primitive schema implementations ignore
		// schema version)
		return decode(bytes);
	}

}
