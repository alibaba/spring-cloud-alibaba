/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.commons.governance.routing.rule;

import java.util.Objects;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class UrlRoutingRule {

	public static class PathRoutingRule implements Rule {

		private final String type = "path";

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
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			PathRoutingRule path = (PathRoutingRule) o;
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
			return "PathRoutingRule{" + "type='" + type + '\'' + ", condition='"
					+ condition + '\'' + ", value='" + value + '\'' + '}';
		}

	}

	public static class ParameterRoutingRule implements Rule {

		private final String type = "parameter";

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
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			ParameterRoutingRule parameterRoutingRule = (ParameterRoutingRule) o;
			return Objects.equals(getType(), parameterRoutingRule.getType())
					&& Objects.equals(getCondition(), parameterRoutingRule.getCondition())
					&& Objects.equals(getKey(), parameterRoutingRule.getKey())
					&& Objects.equals(getValue(), parameterRoutingRule.getValue());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getType(), getCondition(), getKey(), getValue());
		}

		@Override
		public String toString() {
			return "ParameterRoutingRule{" + "type='" + type + '\'' + ", condition='"
					+ condition + '\'' + ", key='" + key + '\'' + ", value='" + value
					+ '\'' + '}';
		}

	}

}
