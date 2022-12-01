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

package com.alibaba.cloud.commons.governance.labelrouting.rule;

import java.util.Objects;

/**
 * @author HH
 */
public class UrlRule {

	public static class Path implements RouteRule {

		private String type;

		private String condition;

		private String value;

		@Override
		public String getCondition() {
			return condition;
		}

		@Override
		public void setCondition(String condition) {
			this.condition = condition;
		}

		@Override
		public String getKey() {
			return null;
		}

		@Override
		public void setKey(String key) {
			//
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String getType() {
			return this.type;
		}

		@Override
		public void setType(String type) {
			this.type = type;
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
			return Objects.equals(getType(), path.getType())
					&& Objects.equals(getCondition(), path.getCondition())
					&& Objects.equals(getValue(), path.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getType(), getCondition(), getValue());
		}

		@Override
		public String toString() {
			return "Path{" + "type='" + type + '\'' + ", condition='" + condition + '\''
					+ ", value='" + value + '\'' + '}';
		}

	}

	public static class Parameter implements RouteRule {

		private String type;

		private String condition;

		private String key;

		private String value;

		@Override
		public String getCondition() {
			return condition;
		}

		@Override
		public void setCondition(String condition) {
			this.condition = condition;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public void setKey(String key) {
			this.key = key;
		}

		@Override
		public String getValue() {
			return value;
		}

		@Override
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public String getType() {
			return this.type;
		}

		@Override
		public void setType(String type) {
			this.type = type;
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
			return Objects.equals(getType(), parameter.getType())
					&& Objects.equals(getCondition(), parameter.getCondition())
					&& Objects.equals(getKey(), parameter.getKey())
					&& Objects.equals(getValue(), parameter.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getType(), getCondition(), getKey(), getValue());
		}

		@Override
		public String toString() {
			return "Parameter{" + "type='" + type + '\'' + ", condition='" + condition
					+ '\'' + ", key='" + key + '\'' + ", value='" + value + '\'' + '}';
		}

	}

}
