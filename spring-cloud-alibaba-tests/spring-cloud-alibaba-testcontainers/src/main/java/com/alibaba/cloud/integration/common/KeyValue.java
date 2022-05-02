package com.alibaba.cloud.integration.common;

import java.nio.ByteBuffer;
import java.util.Objects;

public class KeyValue<K, V> {
	private final K key;
	private final V value;

	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyValue)) {
			return false;
		}
		KeyValue<K, V> another = (KeyValue<K, V>) obj;
		return Objects.equals(key, another.key)
				&& Objects.equals(value, another.value);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(key = \"")
				.append(key)
				.append("\", value = \"")
				.append(value)
				.append("\")");
		return sb.toString();
	}

	/**
	 * Decoder to decode key/value bytes.
	 */
	@FunctionalInterface
	public interface KeyValueDecoder<K, V> {

		/**
		 * Decode key and value bytes into a {@link KeyValue} pair.
		 *
		 * @param keyData key data
		 * @param valueData value data
		 * @return the decoded {@link KeyValue} pair
		 */
		KeyValue<K, V> decode(byte[] keyData, byte[] valueData);

	}

	/**
	 * Encode a <tt>key</tt> and <tt>value</tt> pair into a bytes array.
	 *
	 * @param key key object to encode
	 * @param keyWriter a writer to encode key object
	 * @param value value object to encode
	 * @param valueWriter a writer to encode value object
	 * @return the encoded bytes array
	 */
	public static <K, V> byte[] encode(K key, Schema<K> keyWriter,
									   V value, Schema<V> valueWriter) {
		byte [] keyBytes;
		if (key == null) {
			keyBytes = new byte[0];
		} else {
			keyBytes = keyWriter.encode(key);
		}

		byte [] valueBytes;
		if (value == null) {
			valueBytes = new byte[0];
		} else {
			valueBytes = valueWriter.encode(value);
		}
		ByteBuffer byteBuffer = ByteBuffer.allocate(4 + keyBytes.length + 4 + valueBytes.length);
		byteBuffer
				.putInt(key == null ? -1 : keyBytes.length)
				.put(keyBytes)
				.putInt(value == null ? -1 : valueBytes.length)
				.put(valueBytes);
		return byteBuffer.array();
	}

	/**
	 * Decode the value into a key/value pair.
	 *
	 * @param data the encoded bytes
	 * @param decoder the decoder to decode encoded key/value bytes
	 * @return the decoded key/value pair
	 */
	public static <K, V> KeyValue<K, V> decode(byte[] data, KeyValueDecoder<K, V> decoder) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(data);
		int keyLength = byteBuffer.getInt();
		byte[] keyBytes = keyLength == -1 ? null : new byte[keyLength];
		if (keyBytes != null) {
			byteBuffer.get(keyBytes);
		}

		int valueLength = byteBuffer.getInt();
		byte[] valueBytes = valueLength == -1 ? null : new byte[valueLength];
		if (valueBytes != null) {
			byteBuffer.get(valueBytes);
		}

		return decoder.decode(keyBytes, valueBytes);
	}
}
