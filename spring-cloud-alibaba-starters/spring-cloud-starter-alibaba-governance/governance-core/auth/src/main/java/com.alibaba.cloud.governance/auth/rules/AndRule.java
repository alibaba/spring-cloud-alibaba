package com.alibaba.cloud.governance.auth.rules;

import java.util.ArrayList;
import java.util.List;

public class AndRule<T> {

	private List<OrRule<T>> rules;

	public AndRule(List<OrRule<T>> rules) {
		this.rules = rules;
	}

	public AndRule() {
		this.rules = new ArrayList<>();
	}

	public List<OrRule<T>> getRules() {
		return rules;
	}

	public void addOrRule(OrRule<T> orRule) {
		rules.add(orRule);
	}

	public boolean isEmpty() {
		return rules.isEmpty();
	}

}
