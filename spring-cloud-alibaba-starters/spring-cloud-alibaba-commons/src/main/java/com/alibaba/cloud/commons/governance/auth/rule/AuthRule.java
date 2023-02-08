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

package com.alibaba.cloud.commons.governance.auth.rule;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.cloud.commons.governance.auth.condition.AuthCondition;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 * @since 2.2.10-RC1
 */
public class AuthRule {

	public enum RuleOperation {

		/**
		 * In what way are subrules connected.
		 */
		UNKNOWN, AND, OR

	}

	private RuleOperation op = RuleOperation.UNKNOWN;

	private List<AuthRule> children = new ArrayList<>();

	private AuthCondition condition;

	private boolean isNot;

	public AuthRule() {

	}

	public AuthRule(RuleOperation op) {
		this.op = op;
	}

	public AuthRule(RuleOperation op, boolean isNot) {
		this(op);
		this.isNot = isNot;
	}

	public AuthRule(AuthCondition condition) {
		this.condition = condition;
	}

	public AuthRule(AuthCondition condition, boolean isNot) {
		this(condition);
		this.isNot = isNot;
	}

	public void addChildren(AuthRule rule) {
		children.add(rule);
	}

	public boolean isEmpty() {
		if (children.isEmpty()) {
			return condition == null;
		}
		return false;
	}

	public boolean isLeaf() {
		return condition != null;
	}

	public RuleOperation getOp() {
		return op;
	}

	public void setOp(RuleOperation op) {
		this.op = op;
	}

	public List<AuthRule> getChildren() {
		return children;
	}

	public void setChildren(List<AuthRule> children) {
		this.children = children;
	}

	public AuthCondition getCondition() {
		return condition;
	}

	public void setCondition(AuthCondition condition) {
		this.condition = condition;
	}

	public boolean isNot() {
		return isNot;
	}

	public void setNot(boolean not) {
		isNot = not;
	}

}
