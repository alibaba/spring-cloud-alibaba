/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.common.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResultHolder<T> implements Serializable {

	List<Node> chain = new ArrayList<>();

	private T result;

	/**
	 * Default constructor.
	 */
	public ResultHolder() {

	}

	public ResultHolder(T result) {
		this.result = result;
	}

	public T getResult() {
		return result;
	}

	public void setResult(T result) {
		this.result = result;
	}

	public List<Node> getChain() {
		return chain;
	}

	public void setChain(List<Node> chain) {
		this.chain = chain;
	}

	public void addChain(String app, String unitFlag) {
		chain.add(new Node(app, unitFlag));
	}

	@Override
	public String toString() {
		return "ResultHolder{" + "result=" + result + ", chain=" + chain + '}';
	}

	static class Node implements Serializable {

		private String app;

		private String unitFlag;

		Node() {
		}

		Node(String app, String unitFlag) {
			this.app = app;
			this.unitFlag = unitFlag;
		}

		public String getApp() {
			return app;
		}

		public void setApp(String app) {
			this.app = app;
		}

		public String getUnitFlag() {
			return unitFlag;
		}

		public void setUnitFlag(String unitFlag) {
			this.unitFlag = unitFlag;
		}

		@Override
		public String toString() {
			return "Node{" + "app='" + app + '\'' + ", unitFlag='" + unitFlag + '\''
					+ '}';
		}

	}

}
