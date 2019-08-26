package com.alibaba.cloud.sentinel.rest;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.ClassUtils;

/**
 * Degraded mock data converter
 *
 * @author zkz
 */
public class SentinelRestDegradedMessageConverter
		extends AbstractGenericHttpMessageConverter {
	private static final Logger log = LoggerFactory
			.getLogger(SentinelRestDegradedMessageConverter.class);

	/**
	 * A default constructor that uses {@code "ISO-8859-1"} as the default charset.
	 * @see #AbstractHttpMessageConverter
	 */
	public SentinelRestDegradedMessageConverter() {
		super(SentinelClientHttpResponse.degradedMediaType());
	}

	@Override
	protected Object readInternal(Class clazz, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		try {
			ClientHttpResponse response = (ClientHttpResponse) inputMessage;
			response.getHeaders().setContentType(
					new SentinelClientHttpResponse().getHeaders().getContentType());
			if (clazz == String.class) {
				return response.getStatusText();
			}
			if (ClassUtils.isPrimitiveWrapper(clazz)) {
				return null;
			}
			if (clazz.isArray()) {
				return Array.newInstance(clazz, 0);
			}
			if (clazz.isAssignableFrom(List.class)) {
				return Collections.emptyList();
			}
			if (clazz.isInterface()) {
				return null;
			}
			return clazz.newInstance();
		}
		catch (InstantiationException e) {
			log.warn("Mock data initialization failed after degradation", e);
		}
		catch (IllegalAccessException e) {
			log.warn("Mock data processing failed after degradation", e);
		}
		return null;
	}

	@Override
	protected void writeInternal(Object o, Type type, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {

	}

	@Override
	public Object read(Type type, Class contextClass, HttpInputMessage inputMessage)
			throws IOException, HttpMessageNotReadableException {
		try {
			if (type == null || inputMessage == null) {
				return null;
			}
			return readInternal(
					(Class) GenericTypeResolver.resolveType(type, contextClass),
					inputMessage);
		}
		catch (Exception ex) {
			throw new HttpMessageNotReadableException(
					"Could not resolved Type: " + ex.getMessage(), ex, inputMessage);
		}
	}

	@Override
	protected boolean canWrite(MediaType mediaType) {
		return false;
	}
}
