package com.alibaba.cloud.consumer.feign.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import feign.Response;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public final class FeignConsumerUtil {

	private FeignConsumerUtil(){
	}

	public static String toString(Response response) {

		Reader reader = null;
		StringBuilder to = new StringBuilder();
		CharBuffer charBuf = CharBuffer.allocate(2048);

		try {
			reader = response.body().asReader(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		while(true) {
			try {
				if (reader.read(charBuf) == -1) break;
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
			charBuf.flip();
			to.append(charBuf);
			charBuf.clear();
		}

		return to.toString();
	}

}
