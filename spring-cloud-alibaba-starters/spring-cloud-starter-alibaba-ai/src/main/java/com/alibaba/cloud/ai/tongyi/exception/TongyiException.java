package com.alibaba.cloud.ai.tongyi.exception;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongyiException extends RuntimeException{

	public TongyiException(String message) {

		super(message);
	}

	public TongyiException(String message, Throwable cause) {

		super(message, cause);
	}
}
