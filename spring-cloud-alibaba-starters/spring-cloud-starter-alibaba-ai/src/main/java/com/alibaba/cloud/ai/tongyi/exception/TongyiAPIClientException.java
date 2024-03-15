package com.alibaba.cloud.ai.tongyi.exception;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongyiAPIClientException extends RuntimeException{

	public TongyiAPIClientException(String message) {

		super(message);
	}

	public TongyiAPIClientException(String message, Throwable cause) {

		super(message, cause);
	}
}
