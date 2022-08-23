package com.alibaba.cloud.governance.common.rules;

import java.util.List;

public class OrRule<T> {

	private List<T> rules;

	private boolean not;

	public OrRule(List<T> rules, boolean not) {
		this.rules = rules;
		this.not = not;
	}

	public OrRule(List<T> rules) {
		this(rules, false);
	}

	public List<T> getRules() {
		return rules;
	}

	public boolean isNot() {
		return not;
	}

}
