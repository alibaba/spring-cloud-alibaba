package com.alibaba.cloud.ai.tongyi.exception;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public class TongYiException extends RuntimeException{

	public TongYiException(String message) {

		super(message);
	}

	public TongYiException(String message, Throwable cause) {

		super(message, cause);
	}
}
