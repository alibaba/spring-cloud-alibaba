/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.data.crd.rule;

import java.util.Objects;

/**
 * @author HH
 */
public class UrlRule {
	class Path implements RouteRule {
		private String condition;

		private String value;

		public String getCondition() {
			return condition;
		}

		public void setCondition(String condition) {
			this.condition = condition;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Path path = (Path) o;
			return Objects.equals(getCondition(), path.getCondition()) && Objects
					.equals(getValue(), path.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getCondition(), getValue());
		}

		@Override
		public String toString() {
			return "Path{" +
					"condition='" + condition + '\'' +
					", value='" + value + '\'' +
					'}';
		}
	}

	class Parameter implements RouteRule {
		private String condition;

		private String key;

		private String value;

		public String getCondition() {
			return condition;
		}

		public void setCondition(String condition) {
			this.condition = condition;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Parameter parameter = (Parameter) o;
			return Objects.equals(getCondition(), parameter.getCondition()) && Objects
					.equals(getKey(), parameter.getKey()) && Objects.equals(getValue(), parameter.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getCondition(), getKey(), getValue());
		}

		@Override
		public String toString() {
			return "Parameter{" +
					"condition='" + condition + '\'' +
					", key='" + key + '\'' +
					", value='" + value + '\'' +
					'}';
		}
	}
}
